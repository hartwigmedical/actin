package com.hartwig.actin.clinical.feed.standard.extraction

import SurgeryConfig
import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.feed.datamodel.FeedPatientRecord

class StandardSurgeryExtractor(
    private val surgeryCuration: CurationDatabase<SurgeryConfig>
) : StandardDataExtractor<List<Surgery>> {

    override fun extract(feedPatientRecord: FeedPatientRecord): ExtractionResult<List<Surgery>> {
        return feedPatientRecord.surgeries.map { providedSurgery ->
            val status = providedSurgery.status?.let(SurgeryStatus::valueOf) ?: SurgeryStatus.UNKNOWN

            providedSurgery.name?.takeIf { it.isNotBlank() }?.let { surgery ->
                val curationResponse = CurationResponse.createFromConfigs(
                    surgeryCuration.find(surgery),
                    feedPatientRecord.patientDetails.patientId,
                    CurationCategory.SURGERY,
                    surgery,
                    "surgery"
                )
                val surgery = curationResponse.config()?.takeIf { !it.ignore }?.let {
                    Surgery(
                        name = it.name,
                        endDate = providedSurgery.endDate,
                        status = status,
                        treatmentType = it.treatmentType,
                    )
                }
                ExtractionResult(listOfNotNull(surgery), curationResponse.extractionEvaluation)
            } ?: ExtractionResult(listOf(Surgery(null, providedSurgery.endDate, status, OtherTreatmentType.OTHER_SURGERY)), CurationExtractionEvaluation())
        }
            .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, result ->
                ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
            }
    }
}