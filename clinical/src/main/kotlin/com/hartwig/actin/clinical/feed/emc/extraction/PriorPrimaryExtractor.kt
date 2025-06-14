package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.PriorPrimaryConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.datamodel.clinical.PriorPrimary
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.feed.datamodel.FeedPatientRecord

class PriorPrimaryExtractor(
    private val priorPrimaryCuration: CurationDatabase<PriorPrimaryConfig>,
    private val treatmentHistoryCuration: CurationDatabase<TreatmentHistoryEntryConfig>
) {

    fun extract(patientId: String, feedRecord: FeedPatientRecord): ExtractionResult<List<PriorPrimary>> {
        val curation = listOfNotNull(
            feedRecord.treatmentHistory,
            feedRecord.priorPrimaries
        )
            .asSequence()
            .flatten()
            .map { CurationUtil.fullTrim(it.name) }
            .map {
                CurationResponse.createFromConfigs(
                    priorPrimaryCuration.find(it),
                    patientId,
                    CurationCategory.PRIOR_PRIMARY,
                    it,
                    "second primary or treatment history"
                )
            }
            .map {
                if (it.configs.isEmpty() &&
                    treatmentHistoryCuration.find(it.extractionEvaluation.priorPrimaryEvaluatedInputs.first())
                        .isNotEmpty()
                ) {
                    it.copy(extractionEvaluation = it.extractionEvaluation.copy(warnings = emptySet()))
                } else it
            }
            .fold(CurationResponse<PriorPrimaryConfig>()) { acc, cur -> acc + cur }

        return ExtractionResult(
            curation.configs.filterNot(CurationConfig::ignore).map { it.curated!! },
            curation.extractionEvaluation
        )
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) =
            PriorPrimaryExtractor(
                priorPrimaryCuration = curationDatabaseContext.priorPrimaryCuration,
                treatmentHistoryCuration = curationDatabaseContext.treatmentHistoryEntryCuration
            )
    }
}