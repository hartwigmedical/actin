package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.ToxicityConfig
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.feed.emc.digitalfile.DigitalFileEntry
import com.hartwig.actin.clinical.feed.emc.questionnaire.Questionnaire
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource

class ToxicityExtractor(
    private val toxicityCuration: CurationDatabase<ToxicityConfig>,
    private val toxicityTranslation: TranslationDatabase<String>
) {

    fun extract(
        patientId: String, toxicityEntries: List<DigitalFileEntry>, questionnaire: Questionnaire?
    ): ExtractionResult<List<Toxicity>> {
        val feedToxicities = extractFeedToxicities(toxicityEntries, patientId)

        if (questionnaire != null) {
            val questionnaireToxicities = extractQuestionnaireToxicities(questionnaire, patientId)
            return ExtractionResult(
                feedToxicities.extracted + questionnaireToxicities.extracted,
                feedToxicities.evaluation + questionnaireToxicities.evaluation
            )
        }
        return feedToxicities
    }

    private fun extractFeedToxicities(toxicityEntries: List<DigitalFileEntry>, patientId: String): ExtractionResult<List<Toxicity>> {
        return toxicityEntries.mapNotNull { toxicityEntry ->
            extractGrade(toxicityEntry)?.let { grade ->
                Toxicity(
                    name = toxicityEntry.itemText,
                    evaluatedDate = toxicityEntry.authored,
                    source = ToxicitySource.EHR,
                    grade = grade,
                    categories = emptySet()
                )
            }
        }
            .map { rawToxicity ->
                if (rawToxicity.name.isEmpty()) ExtractionResult(listOf(rawToxicity), CurationExtractionEvaluation()) else {
                    val translationResponse = CurationResponse.createFromTranslation(
                        toxicityTranslation.find(rawToxicity.name),
                        patientId,
                        CurationCategory.TOXICITY_TRANSLATION,
                        rawToxicity.name,
                        "toxicity"
                    )
                    ExtractionResult(
                        listOf(translationResponse.config()?.translated?.let { rawToxicity.copy(name = it) } ?: rawToxicity),
                        translationResponse.extractionEvaluation
                    )
                }
            }
            .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { (toxicities, aggregatedEval), (toxicity, eval) ->
                ExtractionResult(toxicities + toxicity, aggregatedEval + eval)
            }
    }

    private fun extractQuestionnaireToxicities(questionnaire: Questionnaire, patientId: String): ExtractionResult<List<Toxicity>> {
        return questionnaire.unresolvedToxicities?.map { input ->
            val trimmedInput = CurationUtil.fullTrim(input)
            val curationResponse = CurationResponse.createFromConfigs(
                toxicityCuration.find(trimmedInput), patientId, CurationCategory.TOXICITY, trimmedInput, "toxicity"
            )
            val toxicities = curationResponse.configs.filterNot(CurationConfig::ignore).map { config ->
                Toxicity(
                    name = config.name,
                    categories = config.categories,
                    evaluatedDate = questionnaire.date,
                    source = ToxicitySource.QUESTIONNAIRE,
                    grade = config.grade
                )
            }
            ExtractionResult(toxicities, curationResponse.extractionEvaluation)
        }
            ?.fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { (toxicities, aggregatedEval), (toxicity, eval) ->
                ExtractionResult(toxicities + toxicity, aggregatedEval + eval)
            }
            ?: ExtractionResult(emptyList(), CurationExtractionEvaluation())
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
            toxicityCuration = curationDatabaseContext.toxicityCuration,
            toxicityTranslation = curationDatabaseContext.toxicityTranslation
        )
    }
}