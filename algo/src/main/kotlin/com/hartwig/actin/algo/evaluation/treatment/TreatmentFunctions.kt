package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry

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

    data class TreatmentAssessment(
        val hasHadValidTreatment: Boolean = false,
        val hasInconclusiveDate: Boolean = false,
        val hasHadTrialAfterMinDate: Boolean = false
    ) {

        fun combineWith(other: TreatmentAssessment): TreatmentAssessment {
            return TreatmentAssessment(
                hasHadValidTreatment || other.hasHadValidTreatment,
                hasInconclusiveDate || other.hasInconclusiveDate,
                hasHadTrialAfterMinDate || other.hasHadTrialAfterMinDate
            )
        }
    }
}
