package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.ToxicityCuration
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.feed.emc.digitalfile.DigitalFileEntry
import com.hartwig.actin.clinical.feed.emc.questionnaire.Questionnaire
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import java.time.LocalDate

class ToxicityExtractor(
    private val toxicityCuration: CurationDatabase<ComorbidityConfig>,
    private val toxicityTranslation: TranslationDatabase<String>
) {

    fun extract(
        patientId: String, toxicityEntries: List<DigitalFileEntry>, questionnaire: Questionnaire?
    ): ExtractionResult<List<Toxicity>> {
        val feedToxicities = extractFeedToxicities(toxicityEntries, patientId)

        return questionnaire?.unresolvedToxicities?.let { unresolvedToxicities ->
            val questionnaireToxicities = extractQuestionnaireToxicities(unresolvedToxicities, patientId, questionnaire.date)
            ExtractionResult(
                feedToxicities.extracted + questionnaireToxicities.extracted,
                feedToxicities.evaluation + questionnaireToxicities.evaluation
            )
        } ?: feedToxicities
    }

    private fun extractFeedToxicities(toxicityEntries: List<DigitalFileEntry>, patientId: String): ExtractionResult<List<Toxicity>> {
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
            .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { (toxicities, aggregatedEval), (toxicity, eval) ->
                ExtractionResult(toxicities + toxicity, aggregatedEval + eval)
            }
    }

    private fun curatedToxicity(rawToxicity: Toxicity, patientId: String): ExtractionResult<List<Toxicity>>? {
        val input = rawToxicity.name
        val curationResponse = CurationResponse.createFromConfigs(
            toxicityCuration.find(input), patientId, CurationCategory.TOXICITY, input, "toxicity"
        )
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
            toxicityTranslation.find(input),
            patientId,
            CurationCategory.TOXICITY_TRANSLATION,
            input,
            "toxicity"
        ).let { translationResponse ->
            ExtractionResult(
                listOf(translationResponse.config()?.translated?.let { rawToxicity.copy(name = it) } ?: rawToxicity),
                translationResponse.extractionEvaluation
            )
        }
    }

    private fun extractQuestionnaireToxicities(
        unresolvedToxicities: List<String>, patientId: String, questionnaireDate: LocalDate
    ): ExtractionResult<List<Toxicity>> {
        return unresolvedToxicities.map { input ->
            val trimmedInput = CurationUtil.fullTrim(input)
            val curationResponse = CurationResponse.createFromConfigs(
                toxicityCuration.find(trimmedInput), patientId, CurationCategory.TOXICITY, trimmedInput, "toxicity"
            )
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
            .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { (toxicities, aggregatedEval), (toxicity, eval) ->
                ExtractionResult(toxicities + toxicity, aggregatedEval + eval)
            }
    }

    private fun extractGrade(entry: DigitalFileEntry): Int? {
        val value: String = entry.itemAnswerValueValueString.ifEmpty {
            return null
        }
        val notApplicableIndex = value.indexOf(". Not applicable")
        return Integer.valueOf(if (notApplicableIndex > 0) value.substring(0, notApplicableIndex) else value)
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) = ToxicityExtractor(
            toxicityCuration = curationDatabaseContext.comorbidityCuration,
            toxicityTranslation = curationDatabaseContext.toxicityTranslation
        )
    }
}