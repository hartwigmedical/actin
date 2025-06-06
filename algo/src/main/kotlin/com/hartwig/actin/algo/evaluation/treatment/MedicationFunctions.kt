package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentMonth
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentYear

object MedicationFunctions {

    fun Medication.hasCategory(category: TreatmentCategory): Boolean {
        return this.drug?.category == category
    }

    fun Medication.hasDrugType(types: Set<TreatmentType>): Boolean {
        return this.drug?.drugTypes?.any(types::contains) == true
    }

    fun createTreatmentHistoryEntriesFromMedications(medications: List<Medication>?): List<TreatmentHistoryEntry> {
        return medications?.map {
            TreatmentHistoryEntry(
                setOf(DrugTreatment(it.name, setOfNotNull(it.drug))),
                startYear = it.startDate?.year,
                startMonth = it.startDate?.monthValue,
                isTrial = it.isTrialMedication,
                treatmentHistoryDetails = TreatmentHistoryDetails(
                    stopYear = TreatmentYear(it.stopDate?.year),
                    stopMonth = TreatmentMonth(it.stopDate?.monthValue)
                )
            )
        } ?: emptyList()
    }
}