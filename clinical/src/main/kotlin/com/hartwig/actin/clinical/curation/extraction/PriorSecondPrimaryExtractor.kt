package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabases
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.feed.questionnaire.Questionnaire

class PriorSecondPrimaryExtractor(
    private val secondPrimaryCuration: CurationDatabase<SecondPrimaryConfig>,
    private val treatmentHistoryCuration: CurationDatabase<TreatmentHistoryEntryConfig>
) {

    fun extract(patientId: String, questionnaire: Questionnaire?): ExtractionResult<List<PriorSecondPrimary>> {
        if (questionnaire == null) {
            return ExtractionResult(emptyList(), ExtractionEvaluation())
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
                    secondPrimaryCuration.curate(it),
                    patientId,
                    CurationCategory.SECOND_PRIMARY,
                    it,
                    "second primary or treatment history"
                )
            }
            .map {
                if (it.configs.isEmpty() &&
                    treatmentHistoryCuration.curate(it.extractionEvaluation.secondPrimaryEvaluatedInputs.first())
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
        fun create(curationDatabases: CurationDatabases) =
            PriorSecondPrimaryExtractor(
                secondPrimaryCuration = curationDatabases.secondPrimaryCuration,
                treatmentHistoryCuration = curationDatabases.treatmentHistoryEntryCuration
            )
    }
}