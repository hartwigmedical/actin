package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.SurgeryConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.standard.ProvidedPatientRecord
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import java.time.LocalDate

class StandardSurgeryExtractor(
    private val surgeryNameCuration: CurationDatabase<SurgeryConfig>
) : StandardDataExtractor<List<Surgery>> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<Surgery>> {
        val fromSurgeries = ehrPatientRecord.surgeries.map { providedSurgery ->
            if (!providedSurgery.surgeryName.isNullOrBlank()) {
                curateSurgery(
                    ehrPatientRecord,
                    providedSurgery.surgeryName,
                    providedSurgery.endDate,
                    SurgeryStatus.valueOf(providedSurgery.status)
                )
            } else {
                ExtractionResult(
                    listOf(
                        Surgery(
                            name = null,
                            endDate = providedSurgery.endDate,
                            status = SurgeryStatus.valueOf(providedSurgery.status)
                        )
                    ), CurationExtractionEvaluation()
                )
            }
        }
        val fromPriorOtherConditions = ehrPatientRecord.priorOtherConditions.map { curateSurgery(ehrPatientRecord, it.name, it.endDate) }
            .map { ExtractionResult(it.extracted, CurationExtractionEvaluation()) }

        return (fromPriorOtherConditions + fromSurgeries).fold(ExtractionResult(emptyList(), CurationExtractionEvaluation()))
        { acc, result ->
            ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
        }
    }

    private fun curateSurgery(
        ehrPatientRecord: ProvidedPatientRecord,
        name: String,
        endDate: LocalDate? = null,
        surgeryStatus: SurgeryStatus? = null
    ): ExtractionResult<List<Surgery>> {
        val curationResponse = CurationResponse.createFromConfigs(
            surgeryNameCuration.find(name),
            ehrPatientRecord.patientDetails.hashedId,
            CurationCategory.SURGERY,
            name,
            "surgery"
        )
        val surgery = curationResponse.config()?.takeIf { !it.ignore }?.let {
            Surgery(
                name = it.name,
                endDate = endDate ?: it.endDate
                ?: throw IllegalStateException("Surgery $name is not ignored and was curated without an end date. Add the date or ignore this surgery."),
                status = surgeryStatus ?: it.status ?: SurgeryStatus.UNKNOWN
            )
        }
        return ExtractionResult(listOfNotNull(surgery), curationResponse.extractionEvaluation)
    }
}