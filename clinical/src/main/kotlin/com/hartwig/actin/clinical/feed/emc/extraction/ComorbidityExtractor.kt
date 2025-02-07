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
import com.hartwig.actin.clinical.curation.extraction.BooleanValueParser
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.feed.emc.digitalfile.DigitalFileEntry
import com.hartwig.actin.clinical.feed.emc.intolerance.IntoleranceEntry
import com.hartwig.actin.clinical.feed.emc.questionnaire.Questionnaire
import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.Ecg
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
                extractQuestionnaireComorbidities(
                    patientId, it, CurationCategory.NON_ONCOLOGICAL_HISTORY, "non-oncological history", questionnaire.date
                )
            },
            questionnaire?.complications?.let {
                extractQuestionnaireComorbidities(patientId, it, CurationCategory.COMPLICATION, "complication", questionnaire.date)
            },
            extractFeedToxicities(toxicityEntries, patientId),
            questionnaire?.unresolvedToxicities?.let { extractQuestionnaireToxicities(patientId, it, questionnaire.date) },
            extractIntolerances(patientId, intoleranceEntries),
            questionnaire?.ecg?.let { extractEcg(patientId, it) }
        ).flatten()
            .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { (comorbidities, aggregatedEval), (comorbidity, eval) ->
                ExtractionResult(comorbidities + comorbidity, aggregatedEval + eval)
            }
    }

    private fun extractQuestionnaireComorbidities(
        patientId: String, rawInputs: List<String>, category: CurationCategory, configType: String, questionnaireDate: LocalDate
    ): List<ExtractionResult<List<Comorbidity>>> {
        val default = if (category == CurationCategory.COMPLICATION) {
            Complication(null, icdCodes = emptySet())
        } else {
            OtherCondition(null, icdCodes = emptySet())
        }
        return rawInputs.map {
            val curatedComorbidity = curate(CurationUtil.fullTrim(it), patientId, category, configType, default)
            ExtractionResult(
                extracted = curatedComorbidity.configs.mapNotNull(ComorbidityConfig::curated).map { curated ->
                    if (curated is ToxicityCuration) {
                        Toxicity(curated.name, curated.icdCodes, questionnaireDate, ToxicitySource.QUESTIONNAIRE, curated.grade)
                    } else curated
                },
                evaluation = curatedComorbidity.extractionEvaluation
            )
        }
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
                curate(entry.codeText, patientId, CurationCategory.INTOLERANCE, "intolerance", rawIntolerance.copy(name = null), true)

            val curatedIntolerance = curationResponse.config()?.curated?.let { curated ->
                rawIntolerance.copy(name = curated.name, icdCodes = curated.icdCodes)
            } ?: rawIntolerance
            ExtractionResult(listOf(curatedIntolerance), curationResponse.extractionEvaluation)
        }
    }

    private fun extractFeedToxicities(toxicityEntries: List<DigitalFileEntry>, patientId: String): List<ExtractionResult<List<Toxicity>>> {
        return toxicityEntries.mapNotNull { toxicityEntry ->
            extractGrade(toxicityEntry)?.let { grade ->
                val input = toxicityEntry.itemText
                val rawToxicity = Toxicity(
                    name = input,
                    icdCodes = setOf(IcdCode(HARMFUL_EFFECTS_OF_DRUGS_CODE)),
                    evaluatedDate = toxicityEntry.authored,
                    source = ToxicitySource.EHR,
                    grade = grade,
                )
                if (input.isEmpty()) ExtractionResult(listOf(rawToxicity), CurationExtractionEvaluation()) else {
                    curatedToxicity(input, rawToxicity, patientId) ?: translatedToxicity(input, rawToxicity, patientId)
                }
            }
        }
    }

    private fun curatedToxicity(input: String, rawToxicity: Toxicity, patientId: String): ExtractionResult<List<Toxicity>>? {
        val curationResponse = curate(input, patientId, CurationCategory.TOXICITY, "toxicity", rawToxicity.copy(name = null))
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

    private fun translatedToxicity(input: String, rawToxicity: Toxicity, patientId: String): ExtractionResult<List<Toxicity>> {
        return CurationResponse.createFromTranslation(
            toxicityTranslation.find(input), patientId, CurationCategory.TOXICITY_TRANSLATION, input, "toxicity"
        ).let { translationResponse ->
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
        val rawToxicity = Toxicity(
            name = null,
            icdCodes = setOf(IcdCode(HARMFUL_EFFECTS_OF_DRUGS_CODE)),
            evaluatedDate = questionnaireDate,
            source = ToxicitySource.QUESTIONNAIRE,
            grade = null
        )
        return unresolvedToxicities.map { input ->
            val trimmed = CurationUtil.fullTrim(input)
            val curationResponse = curate(trimmed, patientId, CurationCategory.TOXICITY, "toxicity", rawToxicity)
            val toxicities = curationResponse.configs.filterNot(CurationConfig::ignore).mapNotNull { config ->
                config.curated?.let { curated ->
                    rawToxicity.copy(name = curated.name, icdCodes = curated.icdCodes, grade = (curated as? ToxicityCuration)?.grade)
                }
            }
            ExtractionResult(toxicities, curationResponse.extractionEvaluation)
        }
    }

    private fun extractEcg(patientId: String, rawEcg: Ecg): List<ExtractionResult<List<Ecg>>> {
        val curationResponse = rawEcg.name?.let { curate(it, patientId, CurationCategory.ECG, "ECG", rawEcg.copy(name = null), true) }
        val ecg = if (curationResponse?.configs?.size == 0) rawEcg else {
            curationResponse?.config()?.takeUnless { it.ignore }?.curated?.let { curated ->
                rawEcg.copy(
                    name = curated.name,
                    icdCodes = curated.icdCodes,
                    year = curated.year,
                    month = curated.month,
                    qtcfMeasure = coalesce(curated, rawEcg, Ecg::qtcfMeasure),
                    jtcMeasure = coalesce(curated, rawEcg, Ecg::jtcMeasure)
                )
            }
        }
        return listOf(ExtractionResult(listOfNotNull(ecg), curationResponse?.extractionEvaluation ?: CurationExtractionEvaluation()))
    }

    private fun <T, U> coalesce(curated: Comorbidity?, default: T, function: (T) -> U): U =
        curated?.let { it as? T }?.let(function) ?: function(default)

    private fun curate(
        input: String,
        patientId: String,
        category: CurationCategory,
        configType: String,
        defaultForYesInput: Comorbidity,
        requireUniqueness: Boolean = false
    ): CurationResponse<ComorbidityConfig> {
        val trimmed = input.trim()
        return if (BooleanValueParser.isTrue(trimmed)) {
            CurationResponse(
                setOf(ComorbidityConfig(input, false, curated = defaultForYesInput)),
                CurationExtractionEvaluation(comorbidityEvaluatedInputs = setOf(trimmed.lowercase()))
            )
        } else {
            CurationResponse.createFromConfigs(
                comorbidityCuration.find(trimmed),
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