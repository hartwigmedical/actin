package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.PriorPrimaryConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.PriorPrimary
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.feed.datamodel.DatedEntry
import com.hartwig.feed.datamodel.FeedPatientRecord

class StandardPriorPrimariesExtractor(private val priorPrimaryCuration: CurationDatabase<PriorPrimaryConfig>) :
    StandardDataExtractor<List<PriorPrimary>> {
    override fun extract(ehrPatientRecord: FeedPatientRecord): ExtractionResult<List<PriorPrimary>> {
        val priorPrimaries = fromPriorPrimaries(ehrPatientRecord)
        val patientId = ehrPatientRecord.patientDetails.patientId
        val priorPrimariesFromOtherConditions = extractFromSecondarySource(ehrPatientRecord.otherConditions, patientId)
        val priorPrimariesFromTreatmentHistory = extractFromSecondarySource(ehrPatientRecord.treatmentHistory, patientId)
        return (priorPrimaries + priorPrimariesFromOtherConditions + priorPrimariesFromTreatmentHistory).fold(
            ExtractionResult(emptyList(), CurationExtractionEvaluation())
        ) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }

    private fun extractFromSecondarySource(sourceList: List<DatedEntry>, patientId: String): List<ExtractionResult<List<PriorPrimary>>> {
        return sourceList.map { curate(patientId, it.name) }
            .filter { it.configs.isNotEmpty() }
            .map {
                ExtractionResult(it.configs.mapNotNull { c -> c.curated }, it.extractionEvaluation)
            }
    }

    private fun fromPriorPrimaries(ehrPatientRecord: FeedPatientRecord): List<ExtractionResult<List<PriorPrimary>>> =
        ehrPatientRecord.priorPrimaries.map { feedPriorPrimary ->
            val curatedPriorPrimary = curate(ehrPatientRecord.patientDetails.patientId, feedPriorPrimary.name)
            ExtractionResult(
                listOfNotNull(
                    curatedPriorPrimary.config()?.takeUnless { it.ignore }?.curated?.copy(
                        diagnosedMonth = feedPriorPrimary.startDate?.monthValue,
                        diagnosedYear = feedPriorPrimary.startDate?.year,
                        lastTreatmentYear = feedPriorPrimary.endDate?.year,
                        lastTreatmentMonth = feedPriorPrimary.endDate?.monthValue,
                    )
                ), curatedPriorPrimary.extractionEvaluation
            )
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