package com.hartwig.actin.clinical.feed.emc.extraction

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
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
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
        val curatedComorbidity = curate(CurationUtil.fullTrim(it), patientId, category, configType)
        ExtractionResult(
            extracted = curatedComorbidity.configs.mapNotNull(ComorbidityConfig::curated),
            evaluation = curatedComorbidity.extractionEvaluation
        )
    }

    private fun extractIntolerances(
        patientId: String, intoleranceEntries: List<IntoleranceEntry>
    ): List<ExtractionResult<List<Intolerance>>> {
        return intoleranceEntries.map { entry: IntoleranceEntry ->
            with(entry) {
                Intolerance(
                    name = capitalizeFirstLetterOnly(codeText),
                    icdCodes = setOf(IcdCode("", null)),
                    type = capitalizeFirstLetterOnly(isSideEffect),
                    clinicalStatus = capitalizeFirstLetterOnly(clinicalStatus),
                    verificationStatus = capitalizeFirstLetterOnly(verificationStatus),
                    criticality = capitalizeFirstLetterOnly(criticality),
                )
            }
        }
            .map { rawIntolerance ->
                val curationResponse = curate(rawIntolerance.name, patientId, CurationCategory.INTOLERANCE, "intolerance", true)
                val curatedIntolerance = curationResponse.config()?.curated?.let { curated ->
                    rawIntolerance.copy(name = curated.name, icdCodes = curated.icdCodes)
                } ?: rawIntolerance
                ExtractionResult(listOf(curatedIntolerance), curationResponse.extractionEvaluation)
            }
    }

    private fun extractFeedToxicities(toxicityEntries: List<DigitalFileEntry>, patientId: String): List<ExtractionResult<List<Toxicity>>> {
        return toxicityEntries.mapNotNull { toxicityEntry ->
            extractGrade(toxicityEntry)?.let { grade ->
                Toxicity(
                    name = toxicityEntry.itemText,
                    icdCodes = setOf(IcdCode("", null)),
                    evaluatedDate = toxicityEntry.authored,
                    source = ToxicitySource.EHR,
                    grade = grade,
                )
            }
        }
            .map { rawToxicity ->
                if (rawToxicity.name.isEmpty()) ExtractionResult(listOf(rawToxicity), CurationExtractionEvaluation()) else {
                    curatedToxicity(rawToxicity, patientId) ?: translatedToxicity(rawToxicity, patientId)
                }
            }
    }

    private fun curatedToxicity(rawToxicity: Toxicity, patientId: String): ExtractionResult<List<Toxicity>>? {
        val curationResponse = curate(rawToxicity.name, patientId, CurationCategory.TOXICITY, "toxicity")
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
        val input = rawToxicity.name
        return CurationResponse.createFromTranslation(
            toxicityTranslation.find(input), patientId, CurationCategory.TOXICITY_TRANSLATION, input, "toxicity"
        ).let { translationResponse ->
            ExtractionResult(
                listOf(translationResponse.config()?.translated?.let { rawToxicity.copy(name = it) } ?: rawToxicity),
                translationResponse.extractionEvaluation
            )
        }
    }

    private fun extractGrade(entry: DigitalFileEntry): Int? {
        val value: String = entry.itemAnswerValueValueString.ifEmpty {
            return null
        }
        val notApplicableIndex = value.indexOf(". Not applicable")
        return Integer.valueOf(if (notApplicableIndex > 0) value.substring(0, notApplicableIndex) else value)
    }

    private fun extractQuestionnaireToxicities(
        patientId: String, unresolvedToxicities: List<String>, questionnaireDate: LocalDate
    ): List<ExtractionResult<List<Toxicity>>> {
        return unresolvedToxicities.map { input ->
            val curationResponse = curate(CurationUtil.fullTrim(input), patientId, CurationCategory.TOXICITY, "toxicity")
            val toxicities = curationResponse.configs.filterNot(CurationConfig::ignore).mapNotNull { config ->
                config.curated?.let { curated ->
                    Toxicity(
                        name = curated.name,
                        icdCodes = curated.icdCodes,
                        evaluatedDate = questionnaireDate,
                        source = ToxicitySource.QUESTIONNAIRE,
                        grade = (curated as? ToxicityCuration)?.grade
                    )
                }
            }
            ExtractionResult(toxicities, curationResponse.extractionEvaluation)
        }
    }

    private fun curate(
        input: String, patientId: String, category: CurationCategory, configType: String, requireUniqueness: Boolean = false
    ): CurationResponse<ComorbidityConfig> =
        CurationResponse.createFromConfigs(comorbidityCuration.find(input), patientId, category, input, configType, requireUniqueness)

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) =
            ComorbidityExtractor(
                comorbidityCuration = curationDatabaseContext.comorbidityCuration,
                toxicityTranslation = curationDatabaseContext.toxicityTranslation
            )
    }
}