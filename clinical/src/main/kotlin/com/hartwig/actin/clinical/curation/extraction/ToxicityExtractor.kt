package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import com.hartwig.actin.clinical.feed.digitalfile.DigitalFileEntry
import com.hartwig.actin.clinical.feed.questionnaire.Questionnaire

class ToxicityExtractor(private val curation: CurationDatabase) {

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
                ImmutableToxicity.builder()
                    .name(toxicityEntry.itemText)
                    .evaluatedDate(toxicityEntry.authored)
                    .source(ToxicitySource.EHR)
                    .grade(grade)
                    .build()
            }
        }
            .map {
                val translationResponse = CurationResponse.createFromTranslation(
                    curation.translateToxicity(it.name()), patientId, CurationCategory.TOXICITY_TRANSLATION, it.name(), "toxicity"
                )
                ExtractionResult(
                    listOf(translationResponse.config()?.let { translated -> it.withName(translated) } ?: it),
                    translationResponse.extractionEvaluation
                )
            }
            .fold(ExtractionResult(emptyList(), ExtractionEvaluation())) { (toxicities, aggregatedEval), (toxicity, eval) ->
                ExtractionResult(toxicities + toxicity, aggregatedEval + eval)
            }
    }

    private fun extractQuestionnaireToxicities(questionnaire: Questionnaire, patientId: String): ExtractionResult<List<Toxicity>> {
        return questionnaire.unresolvedToxicities?.map { input ->
            val trimmedInput = CurationUtil.fullTrim(input)
            val curationResponse = CurationResponse.createFromConfigs(
                curation.findToxicityConfigs(trimmedInput), patientId, CurationCategory.TOXICITY, trimmedInput, "toxicity"
            )
            val toxicities = curationResponse.configs.filterNot(CurationConfig::ignore).map { config ->
                ImmutableToxicity.builder()
                    .name(config.name)
                    .categories(config.categories)
                    .evaluatedDate(questionnaire.date)
                    .source(ToxicitySource.QUESTIONNAIRE)
                    .grade(config.grade)
                    .build()
            }
            ExtractionResult(toxicities, curationResponse.extractionEvaluation)
        }
            ?.fold(ExtractionResult(emptyList(), ExtractionEvaluation())) { (toxicities, aggregatedEval), (toxicity, eval) ->
                ExtractionResult(toxicities + toxicity, aggregatedEval + eval)
            }
            ?: ExtractionResult(emptyList(), ExtractionEvaluation())
    }

    private fun extractGrade(entry: DigitalFileEntry): Int? {
        val value: String = entry.itemAnswerValueValueString.ifEmpty {
            return null
        }
        val notApplicableIndex = value.indexOf(". Not applicable")
        return Integer.valueOf(if (notApplicableIndex > 0) value.substring(0, notApplicableIndex) else value)
    }
}