package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.SurgeryNameConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientRecord
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus

class StandardSurgeryExtractor(
    private val surgeryNameCuration: CurationDatabase<SurgeryNameConfig>
) : StandardDataExtractor<List<Surgery>> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<Surgery>> {
        return ehrPatientRecord.surgeries.map { providedSurgery ->
            providedSurgery.surgeryName?.takeIf { it.isNotBlank() }?.let { surgeryName ->
                val curationResponse = CurationResponse.createFromConfigs(
                    surgeryNameCuration.find(surgeryName),
                    ehrPatientRecord.patientDetails.hashedId,
                    CurationCategory.SURGERY_NAME,
                    surgeryName,
                    "surgery"
                )
                val surgery = curationResponse.config()?.takeIf { !it.ignore }?.let {
                    Surgery(
                        name = it.name,
                        endDate = providedSurgery.endDate,
                        status = SurgeryStatus.valueOf(providedSurgery.status)
                    )
                }
                ExtractionResult(listOfNotNull(surgery), curationResponse.extractionEvaluation)
            }
                ?: ExtractionResult(
                    listOf(Surgery(name = null, endDate = providedSurgery.endDate, status = SurgeryStatus.valueOf(providedSurgery.status))),
                    CurationExtractionEvaluation()
                )
        }.fold(ExtractionResult(emptyList(), CurationExtractionEvaluation()))
        { acc, result ->
            ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
        }
    }
}