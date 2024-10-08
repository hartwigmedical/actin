package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

object TreatmentFunctions {

    fun receivedPlatinumDoublet(record: PatientRecord): Boolean {
        return record.oncologicalHistory.any { entry ->
            val chemotherapyDrugs = createChemotherapyDrugList(entry)
            chemotherapyDrugs.size == 2 && chemotherapyDrugs.any { it.drugTypes.contains(DrugType.PLATINUM_COMPOUND) }
        }
    }

    fun receivedPlatinumTripletOrAbove(record: PatientRecord): Boolean {
        return record.oncologicalHistory.any { entry ->
            val chemotherapyDrugs = createChemotherapyDrugList(entry)
            chemotherapyDrugs.size > 2 && chemotherapyDrugs.any { it.drugTypes.contains(DrugType.PLATINUM_COMPOUND) }
        }
    }

    private fun createChemotherapyDrugList(entry: TreatmentHistoryEntry): List<Drug> {
        return entry.treatments.filterIsInstance<DrugTreatment>()
            .flatMap(DrugTreatment::drugs)
            .filter { it.category == TreatmentCategory.CHEMOTHERAPY }
    }

    fun createTreatmentHistoryEntriesFromMedications(medications: List<Medication>?): List<TreatmentHistoryEntry> {
        return medications?.map {
            TreatmentHistoryEntry(
                setOf(DrugTreatment(it.name, setOfNotNull(it.drug))),
                isTrial = it.isTrialMedication
            )
        } ?: emptyList()
    }
}
