package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.emc.questionnaire.Questionnaire
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition

class PriorOtherConditionsExtractor(private val nonOncologicalCuration: CurationDatabase<NonOncologicalHistoryConfig>) {

    fun extract(patientId: String, questionnaire: Questionnaire?): ExtractionResult<List<PriorOtherCondition>> {
        if (questionnaire?.nonOncologicalHistory == null) {
            return ExtractionResult(emptyList(), CurationExtractionEvaluation())
        }
        val curation = questionnaire.nonOncologicalHistory
            .asSequence()
            .map(CurationUtil::fullTrim)
            .map {
                CurationResponse.createFromConfigs(
                    nonOncologicalCuration.find(it),
                    patientId,
                    CurationCategory.NON_ONCOLOGICAL_HISTORY,
                    it,
                    "non-oncological history"
                )
            }
            .fold(CurationResponse<NonOncologicalHistoryConfig>()) { acc, cur -> acc + cur }
        val priorOtherConditions = curation.configs.filterNot { it.ignore }
            .mapNotNull { it.priorOtherCondition }
        return ExtractionResult(priorOtherConditions, curation.extractionEvaluation)
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) =
            PriorOtherConditionsExtractor(nonOncologicalCuration = curationDatabaseContext.nonOncologicalHistoryCuration)
    }
}