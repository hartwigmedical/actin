package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.algo.icd.IcdConstants.ALLERGIC_OR_HYPERSENSITIVITY_CONDITIONS_OF_UNSPECIFIED_TYPE
import com.hartwig.actin.algo.icd.IcdConstants.HARMFUL_EFFECTS_OF_DRUGS_CODE
import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.CurationUtil.capitalizeFirstLetterOnly
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.ToxicityCuration
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.feed.emc.digitalfile.DigitalFileEntry
import com.hartwig.actin.clinical.feed.emc.intolerance.IntoleranceEntry
import com.hartwig.actin.clinical.feed.emc.questionnaire.Questionnaire
import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.OtherCondition
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import java.time.LocalDate
import kotlin.collections.plus

class ComorbidityExtractor(
    private val comorbidityCuration: CurationDatabase<ComorbidityConfig>,
    private val toxicityTranslation: TranslationDatabase<String>
) {

    fun extract(
        patientId: String,
        questionnaire: Questionnaire?,
        toxicityEntries: List<DigitalFileEntry>,
        intoleranceEntries: List<IntoleranceEntry>
    ): ExtractionResult<List<Comorbidity>> {
        return listOfNotNull(
            questionnaire?.nonOncologicalHistory?.let {
                extractQuestionnaireComorbidities(patientId, it, CurationCategory.NON_ONCOLOGICAL_HISTORY, "non-oncological history")
            },
            questionnaire?.complications?.let {
                extractQuestionnaireComorbidities(patientId, it, CurationCategory.COMPLICATION, "complication")
            },
            extractFeedToxicities(toxicityEntries, patientId),
            questionnaire?.unresolvedToxicities?.let { extractQuestionnaireToxicities(patientId, it, questionnaire.date) },
            extractIntolerances(patientId, intoleranceEntries)
        ).flatten()
            .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { (comorbidities, aggregatedEval), (comorbidity, eval) ->
                ExtractionResult(comorbidities + comorbidity, aggregatedEval + eval)
            }
    }

    private fun extractQuestionnaireComorbidities(
        patientId: String, rawInputs: List<String>, category: CurationCategory, configType: String
    ): List<ExtractionResult<List<Comorbidity>>> = rawInputs.map {
        val emptyIcd = setOf(IcdCode(""))
        val default = if (category == CurationCategory.COMPLICATION) {
            Complication("", icdCodes = emptyIcd)
        } else {
            OtherCondition("", icdCodes = emptyIcd)
        }
        val curatedComorbidity = curate(CurationUtil.fullTrim(it), patientId, category, configType, default)
        ExtractionResult(
            extracted = curatedComorbidity.configs.mapNotNull(ComorbidityConfig::curated),
            evaluation = curatedComorbidity.extractionEvaluation
        )
    }

    private fun extractIntolerances(
        patientId: String, intoleranceEntries: List<IntoleranceEntry>
    ): List<ExtractionResult<List<Intolerance>>> {
        return intoleranceEntries.map { entry: IntoleranceEntry ->
            val rawIntolerance = with(entry) {
                Intolerance(
                    name = capitalizeFirstLetterOnly(codeText),
                    icdCodes = setOf(IcdCode(ALLERGIC_OR_HYPERSENSITIVITY_CONDITIONS_OF_UNSPECIFIED_TYPE)),
                    type = capitalizeFirstLetterOnly(isSideEffect),
                    clinicalStatus = capitalizeFirstLetterOnly(clinicalStatus),
                    verificationStatus = capitalizeFirstLetterOnly(verificationStatus),
                    criticality = capitalizeFirstLetterOnly(criticality),
                )
            }
            val curationResponse =
                curate(rawIntolerance.name, patientId, CurationCategory.INTOLERANCE, "intolerance", rawIntolerance.copy(name = ""), true)

            val curatedIntolerance = curationResponse.config()?.curated?.let { curated ->
                rawIntolerance.copy(name = curated.name, icdCodes = curated.icdCodes)
            } ?: rawIntolerance
            ExtractionResult(listOf(curatedIntolerance), curationResponse.extractionEvaluation)
        }
    }

    private fun extractFeedToxicities(toxicityEntries: List<DigitalFileEntry>, patientId: String): List<ExtractionResult<List<Toxicity>>> {
        return toxicityEntries.mapNotNull { toxicityEntry ->
            extractGrade(toxicityEntry)?.let { grade ->
                val rawToxicity = Toxicity(
                    name = toxicityEntry.itemText,
                    icdCodes = setOf(IcdCode(HARMFUL_EFFECTS_OF_DRUGS_CODE)),
                    evaluatedDate = toxicityEntry.authored,
                    source = ToxicitySource.EHR,
                    grade = grade,
                )
                if (rawToxicity.name.isEmpty()) ExtractionResult(listOf(rawToxicity), CurationExtractionEvaluation()) else {
                    curatedToxicity(rawToxicity, patientId) ?: translatedToxicity(rawToxicity, patientId)
                }
            }
        }
    }

    private fun curatedToxicity(rawToxicity: Toxicity, patientId: String): ExtractionResult<List<Toxicity>>? {
        val curationResponse = curate(rawToxicity.name, patientId, CurationCategory.TOXICITY, "toxicity", rawToxicity.copy(name = ""))
        return curationResponse.config()?.curated?.let { curated ->
            ExtractionResult(
                listOf(
                    rawToxicity.copy(
                        name = curated.name,
                        icdCodes = curated.icdCodes,
                        grade = (curated as? ToxicityCuration)?.grade ?: rawToxicity.grade
                    )
                ),
                curationResponse.extractionEvaluation
            )
        }
    }

    private fun translatedToxicity(rawToxicity: Toxicity, patientId: String): ExtractionResult<List<Toxicity>> {
        return rawToxicity.name.let { input ->
            CurationResponse.createFromTranslation(
                toxicityTranslation.find(input), patientId, CurationCategory.TOXICITY_TRANSLATION, input, "toxicity"
            )
        }.let { translationResponse ->
            ExtractionResult(
                listOf(translationResponse.config()?.translated?.let { rawToxicity.copy(name = it) } ?: rawToxicity),
                translationResponse.extractionEvaluation
            )
        }
    }

    private fun extractGrade(entry: DigitalFileEntry): Int? = entry.itemAnswerValueValueString.takeUnless { it.isEmpty() }?.let { value ->
        val notApplicableIndex = value.indexOf(". Not applicable")
        return Integer.valueOf(if (notApplicableIndex > 0) value.substring(0, notApplicableIndex) else value)
    }

    private fun extractQuestionnaireToxicities(
        patientId: String, unresolvedToxicities: List<String>, questionnaireDate: LocalDate
    ): List<ExtractionResult<List<Toxicity>>> {
        return unresolvedToxicities.map { input ->
            val trimmed = CurationUtil.fullTrim(input)
            val toxicity = Toxicity(
                name = trimmed,
                icdCodes = setOf(IcdCode(HARMFUL_EFFECTS_OF_DRUGS_CODE)),
                evaluatedDate = questionnaireDate,
                source = ToxicitySource.QUESTIONNAIRE,
                grade = null
            )
            val curationResponse = curate(trimmed, patientId, CurationCategory.TOXICITY, "toxicity", toxicity.copy(name = ""))
            val toxicities = curationResponse.configs.filterNot(CurationConfig::ignore).mapNotNull { config ->
                config.curated?.let { curated ->
                    toxicity.copy(name = curated.name, icdCodes = curated.icdCodes, grade = (curated as? ToxicityCuration)?.grade)
                }
            }
            ExtractionResult(toxicities, curationResponse.extractionEvaluation)
        }
    }

    private fun curate(
        input: String,
        patientId: String,
        category: CurationCategory,
        configType: String,
        defaultForYesInput: Comorbidity,
        requireUniqueness: Boolean = false
    ): CurationResponse<ComorbidityConfig> {
        val trimmed = input.trim()
        return if (trimmed.equals("yes", ignoreCase = true)) {
            CurationResponse(
                setOf(ComorbidityConfig(input, false, curated = defaultForYesInput)),
                CurationExtractionEvaluation(comorbidityEvaluatedInputs = setOf(trimmed.lowercase()))
            )
        } else {
            CurationResponse.createFromConfigs(
                comorbidityCuration.find(input),
                patientId,
                category,
                input,
                configType,
                requireUniqueness
            )
        }
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) =
            ComorbidityExtractor(
                comorbidityCuration = curationDatabaseContext.comorbidityCuration,
                toxicityTranslation = curationDatabaseContext.toxicityTranslation
            )
    }
}