package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

object MedicationFunctions {

    fun Medication.hasCategory(category: TreatmentCategory): Boolean {
        return this.drug?.category == category
    }

    fun Medication.hasDrugType(types: Set<TreatmentType>): Boolean {
        return this.drug?.drugTypes?.any(types::contains) == true
    }
}