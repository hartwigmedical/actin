package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.emc.questionnaire.Questionnaire
import com.hartwig.actin.datamodel.clinical.PriorSecondPrimary

class PriorSecondPrimaryExtractor(
    private val secondPrimaryCuration: CurationDatabase<SecondPrimaryConfig>,
    private val treatmentHistoryCuration: CurationDatabase<TreatmentHistoryEntryConfig>
) {

    fun extract(patientId: String, questionnaire: Questionnaire?): ExtractionResult<List<PriorSecondPrimary>> {
        if (questionnaire == null) {
            return ExtractionResult(emptyList(), CurationExtractionEvaluation())
        }
        val curation = listOfNotNull(
            questionnaire.otherOncologicalHistory,
            questionnaire.secondaryPrimaries
        )
            .asSequence()
            .flatten()
            .map(CurationUtil::fullTrim)
            .map {
                CurationResponse.createFromConfigs(
                    secondPrimaryCuration.find(it),
                    patientId,
                    CurationCategory.SECOND_PRIMARY,
                    it,
                    "second primary or treatment history"
                )
            }
            .map {
                if (it.configs.isEmpty() &&
                    treatmentHistoryCuration.find(it.extractionEvaluation.secondPrimaryEvaluatedInputs.first())
                        .isNotEmpty()
                ) {
                    it.copy(extractionEvaluation = it.extractionEvaluation.copy(warnings = emptySet()))
                } else it
            }
            .fold(CurationResponse<SecondPrimaryConfig>()) { acc, cur -> acc + cur }

        return ExtractionResult(
            curation.configs.filterNot(CurationConfig::ignore).map { it.curated!! },
            curation.extractionEvaluation
        )
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) =
            PriorSecondPrimaryExtractor(
                secondPrimaryCuration = curationDatabaseContext.secondPrimaryCuration,
                treatmentHistoryCuration = curationDatabaseContext.treatmentHistoryEntryCuration
            )
    }
}