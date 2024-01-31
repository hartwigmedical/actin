package com.hartwig.actin.clinical.nki

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentStage

class EhrTreatmentHistoryExtractor(
    private val treatmentDatabase: TreatmentDatabase
) : EhrExtractor<List<TreatmentHistoryEntry>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<TreatmentHistoryEntry>> {
        val extracted = ehrPatientRecord.treatmentHistory.map {

            val treatment = treatmentDatabase.findTreatmentByName(it.treatmentName)

            val switchToTreatments = it.modifications.map { modification ->
                val modificationTreatment = treatmentDatabase.findTreatmentByName(modification.name)
                modificationTreatment?.let { t ->
                    Pair(
                        TreatmentStage(
                            cycles = modification.administeredCycles,
                            startYear = modification.date.year,
                            startMonth = modification.date.monthValue,
                            treatment = t
                        ), emptySet()
                    )
                } ?: Pair(
                    null,
                    setOf(
                        CurationWarning(
                            ehrPatientRecord.patientDetails.patientId,
                            CurationCategory.ONCOLOGICAL_HISTORY,
                            modification.name,
                            "Treatment ${modification.name} not found in database"
                        )
                    )
                )
            }
            val historyDetails =
                TreatmentHistoryDetails(
                    stopYear = it.endDate.year,
                    stopMonth = it.endDate.monthValue,
                    stopReason = StopReason.valueOf(it.stopReason.name),
                    bestResponse = TreatmentResponse.valueOf(it.response.name),
                    switchToTreatments = switchToTreatments.mapNotNull { switch -> switch.first },
                    cycles = it.administeredCycles,
                )
            Pair(
                TreatmentHistoryEntry(
                    startYear = it.startDate.year,
                    startMonth = it.startDate.monthValue,
                    intents = setOf(Intent.valueOf(it.intention.name)),
                    treatments = setOfNotNull(treatment),
                    treatmentHistoryDetails = historyDetails,
                    isTrial = it.administeredInStudy
                ), switchToTreatments.map { switch -> switch.second }.flatten().toSet()
            )
        }
        return ExtractionResult(extracted.map { it.first }, ExtractionEvaluation(warnings = extracted.map { it.second }.flatten().toSet()))
    }
}