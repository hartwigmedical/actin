package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.MedicationStatus
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

object MedicationToTreatmentConverter {

    fun convert(medications: List<Medication>, treatmentHistory: List<TreatmentHistoryEntry>): List<TreatmentHistoryEntry> {
        val treatmentsByDrug = createTreatmentHistoryEntryPerDrugMap(treatmentHistory)
        return medications.filter { medication ->
            val isSystemicCancerTreatment = medication.drug?.category in TreatmentCategory.SYSTEMIC_CANCER_TREATMENT_CATEGORIES
            val hasNoMatchingTreatmentHistoryEntry =
                medication.drug?.let(treatmentsByDrug::get)?.none { matchesDate(medication, it) } ?: true
            val mayBeActive = medication.status == null || medication.status == MedicationStatus.ACTIVE
            isSystemicCancerTreatment && hasNoMatchingTreatmentHistoryEntry && mayBeActive
        }
            .groupBy { it.drug }
            .mapNotNull { (drug, medications) ->
                val (start, stop) = extractStartAndStopRange(medications)
                val name = drug?.name?.lowercase()?.replaceFirstChar { char -> char.uppercase() } ?: "Unknown"
                TreatmentHistoryEntry(
                    startYear = start?.year,
                    startMonth = start?.monthValue,
                    treatments = setOf(DrugTreatment(name = name, drugs = setOfNotNull(drug))),
                    treatmentHistoryDetails = TreatmentHistoryDetails(stopYear = stop?.year, stopMonth = stop?.monthValue)
                )
            }
    }

    private fun createTreatmentHistoryEntryPerDrugMap(treatmentHistory: List<TreatmentHistoryEntry>): Map<Drug, List<TreatmentHistoryEntry>> {
        return treatmentHistory.flatMap { entry ->
            entry.allTreatments().flatMap { treatment ->
                (treatment as? DrugTreatment)?.drugs?.map { it to entry } ?: emptyList()
            }
        }
            .groupBy({ it.first }, { it.second })
    }

    private fun matchesDate(medication: Medication, treatmentHistory: TreatmentHistoryEntry): Boolean {
        val medicationStart = medication.startDate
        val medicationStop = medication.stopDate
        val treatmentStart = treatmentHistory.startYear?.let { LocalDate.of(it, treatmentHistory.startMonth ?: 1, 1) }
        val treatmentStop = treatmentHistory.treatmentHistoryDetails?.stopYear?.let {
            LocalDate.of(it, treatmentHistory.treatmentHistoryDetails?.stopMonth ?: 12, 31)
        }

        return (medicationStart?.isAfter(treatmentStart) == true && (medicationStop?.isBefore(treatmentStop) == true ||
                medicationStart.isBefore(treatmentStop))) || treatmentStart == null
    }

    private fun extractStartAndStopRange(medications: List<Medication>): Pair<LocalDate?, LocalDate?> {
        val oldestStartDate = medications.mapNotNull { it.startDate }.minOrNull()
        val newestStopDate = medications.mapNotNull { it.stopDate }.maxOrNull()

        return Pair(oldestStartDate, newestStopDate)
    }
}