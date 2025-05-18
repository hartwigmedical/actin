package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.SurgeryNameConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import com.hartwig.feed.datamodel.FeedPatientRecord

class StandardSurgeryExtractor(
    private val surgeryNameCuration: CurationDatabase<SurgeryNameConfig>
) : StandardDataExtractor<List<Surgery>> {

    override fun extract(ehrPatientRecord: FeedPatientRecord): ExtractionResult<List<Surgery>> {
        return ehrPatientRecord.surgeries.map { providedSurgery ->
            val status = providedSurgery.status?.let(SurgeryStatus::valueOf) ?: SurgeryStatus.UNKNOWN

            providedSurgery.name?.takeIf { it.isNotBlank() }?.let { surgeryName ->
                val curationResponse = CurationResponse.createFromConfigs(
                    surgeryNameCuration.find(surgeryName),
                    ehrPatientRecord.patientDetails.patientId,
                    CurationCategory.SURGERY_NAME,
                    surgeryName,
                    "surgery"
                )
                val surgery = curationResponse.config()?.takeIf { !it.ignore }?.let {
                    Surgery(
                        name = it.name,
                        endDate = providedSurgery.endDate,
                        status = status
                    )
                }
                ExtractionResult(listOfNotNull(surgery), curationResponse.extractionEvaluation)
            } ?: ExtractionResult(listOf(Surgery(null, providedSurgery.endDate, status)), CurationExtractionEvaluation())
        }
            .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, result ->
                ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
            }
    }
}