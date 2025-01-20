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
import com.hartwig.actin.datamodel.clinical.OtherCondition

class OtherConditionsExtractor(private val nonOncologicalCuration: CurationDatabase<NonOncologicalHistoryConfig>) {

    fun extract(patientId: String, questionnaire: Questionnaire?): ExtractionResult<List<OtherCondition>> {
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
        val otherConditions = curation.configs.filterNot { it.ignore }
            .mapNotNull { it.otherCondition }
        return ExtractionResult(otherConditions, curation.extractionEvaluation)
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) =
            OtherConditionsExtractor(nonOncologicalCuration = curationDatabaseContext.nonOncologicalHistoryCuration)
    }
}