package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.PriorPrimaryConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.PriorPrimary
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.feed.datamodel.FeedPatientRecord

class StandardPriorPrimariesExtractor(private val priorPrimaryCuration: CurationDatabase<PriorPrimaryConfig>) :
    StandardDataExtractor<List<PriorPrimary>> {
    override fun extract(feedPatientRecord: FeedPatientRecord): ExtractionResult<List<PriorPrimary>> {
        return feedPatientRecord.priorPrimaries.map { feedPriorPrimary ->
            val curatedPriorPrimary = curate(feedPatientRecord.patientDetails.patientId, feedPriorPrimary.name)
            ExtractionResult(
                curatedPriorPrimary.configs.filterNot { it.ignore }.mapNotNull {
                    it.curated?.copy(
                        diagnosedMonth = feedPriorPrimary.startDate?.monthValue,
                        diagnosedYear = feedPriorPrimary.startDate?.year,
                        lastTreatmentYear = feedPriorPrimary.endDate?.year,
                        lastTreatmentMonth = feedPriorPrimary.endDate?.monthValue,
                    )
                },
                curatedPriorPrimary.extractionEvaluation
            )
        }.fold(
            ExtractionResult(emptyList(), CurationExtractionEvaluation())
        ) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }

    private fun curate(patientId: String, input: String) = CurationResponse.createFromConfigs(
        priorPrimaryCuration.find(input),
        patientId,
        CurationCategory.PRIOR_PRIMARY,
        input,
        "prior primary",
        false
    )
}