package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

object MedicationFunctions {

    fun hasCategory(medication: Medication, category: TreatmentCategory): Boolean {
        return medication.drug?.category?.equals(category) == true
    }

    fun hasDrugType(medication: Medication, types: Set<TreatmentType>): Boolean {
        return medication.drug?.drugTypes?.any { types.contains(it) } == true
    }

    fun doesNotHaveIgnoreType(medication: Medication, ignoreTypes: Set<TreatmentType>): Boolean {
        return medication.drug?.drugTypes?.any { !ignoreTypes.contains(it) } == true
    }
}