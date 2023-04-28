package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.clinical.datamodel.Medication

internal object MedicationDosage {
    fun hasMatchingDosing(medication1: Medication, medication2: Medication): Boolean {
        return if (hasDosing(medication1) && hasDosing(medication2)) {
            val dosageMinMatch = (medication1.dosageMin()!!).compareTo(medication2.dosageMin()!!) == 0
            val dosageMaxMatch = (medication1.dosageMax()!!).compareTo(medication2.dosageMax()!!) == 0
            val dosageUnitMatch = medication1.dosageUnit() == medication2.dosageUnit()
            val frequencyMatch = (medication1.frequency()!!).compareTo(medication2.frequency()!!) == 0
            val frequencyUnitMatch = medication1.frequencyUnit() == medication2.frequencyUnit()
            val ifNeededMatch = medication1.ifNeeded() == medication2.ifNeeded()
            dosageMinMatch && dosageMaxMatch && dosageUnitMatch && frequencyMatch && frequencyUnitMatch && ifNeededMatch
        } else {
            false
        }
    }

    private fun hasDosing(medication: Medication): Boolean {
        return medication.dosageMin() != null && medication.dosageMax() != null && medication.dosageUnit() != null && medication.frequency() != null && medication.frequencyUnit() != null && medication.ifNeeded() != null
    }
}