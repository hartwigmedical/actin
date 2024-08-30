package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.feed.emc.questionnaire.Questionnaire
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

class OncologicalHistoryExtractor(
    private val treatmentHistoryCuration: CurationDatabase<TreatmentHistoryEntryConfig>,
    private val secondPrimaryCuration: CurationDatabase<SecondPrimaryConfig>
) {

    fun extract(patientId: String, questionnaire: Questionnaire?): ExtractionResult<List<TreatmentHistoryEntry>> {
        if (questionnaire == null) {
            return ExtractionResult(emptyList(), CurationExtractionEvaluation())
        }
        val treatmentHistoryCuration = listOfNotNull(
            questionnaire.treatmentHistoryCurrentTumor,
            questionnaire.otherOncologicalHistory
        )
            .asSequence()
            .flatten()
            .map(CurationUtil::fullTrim)
            .map {
                CurationResponse.createFromConfigs(
                    treatmentHistoryCuration.find(it),
                    patientId,
                    CurationCategory.ONCOLOGICAL_HISTORY,
                    it,
                    "treatment history or second primary"
                )
            }
            .map {
                if (it.configs.isEmpty() &&
                    secondPrimaryCuration.find(it.extractionEvaluation.treatmentHistoryEntryEvaluatedInputs.first())
                        .isNotEmpty()
                ) {
                    it.copy(extractionEvaluation = it.extractionEvaluation.copy(warnings = emptySet()))
                } else it
            }
            .fold(CurationResponse<TreatmentHistoryEntryConfig>()) { acc, cur -> acc + cur }

        return ExtractionResult(
            treatmentHistoryCuration.configs.filterNot(CurationConfig::ignore).map { it.curated!! },
            treatmentHistoryCuration.extractionEvaluation
        )
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) =
            OncologicalHistoryExtractor(
                treatmentHistoryCuration = curationDatabaseContext.treatmentHistoryEntryCuration,
                secondPrimaryCuration = curationDatabaseContext.secondPrimaryCuration
            )
    }
}