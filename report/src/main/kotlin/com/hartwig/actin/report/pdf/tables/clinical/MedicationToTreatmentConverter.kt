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
            val hasMatchingTreatmentHistoryEntry = treatmentsByDrug[medication.drug]?.any { matchesDate(medication, it) } == true
            val isSystemicCancerTreatment = medication.drug?.category in TreatmentCategory.SYSTEMIC_CANCER_TREATMENT_CATEGORIES
            val mayBeActive = medication.status == null || medication.status == MedicationStatus.ACTIVE
            (medication.drug?.let { _ -> !hasMatchingTreatmentHistoryEntry } ?: false) && isSystemicCancerTreatment && mayBeActive
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
        if (medication.startDate?.year == treatmentHistory.startYear || medication.startDate?.year == treatmentHistory.treatmentHistoryDetails?.stopYear) {
            val values =
                medication.startDate?.let { (it.monthValue..(medication.stopDate?.monthValue ?: it.monthValue)).toList() } ?: emptyList()
            if (treatmentHistory.startMonth in values || treatmentHistory.startMonth == null || treatmentHistory.treatmentHistoryDetails?.stopMonth in values) {
                return true
            }
        }
        return false
    }

    private fun extractStartAndStopRange(medications: List<Medication>): Pair<LocalDate?, LocalDate?> {
        val oldestStartDate = medications.mapNotNull { it.startDate }.minOrNull()
        val newestStopDate = medications.mapNotNull { it.stopDate }.maxOrNull()

        return Pair(oldestStartDate, newestStopDate)
    }
}