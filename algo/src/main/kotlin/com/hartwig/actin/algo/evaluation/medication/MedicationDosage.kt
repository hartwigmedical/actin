package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.clinical.datamodel.Dosage

internal object MedicationDosage {
    fun hasMatchingDosing(dosage1: Dosage, dosage2: Dosage): Boolean {
        return if (hasDosing(dosage1) && hasDosing(dosage2)) {
            val dosageMinMatch = (dosage1.dosageMin()!!).compareTo(dosage2.dosageMin()!!) == 0
            val dosageMaxMatch = (dosage1.dosageMax()!!).compareTo(dosage2.dosageMax()!!) == 0
            val dosageUnitMatch = dosage1.dosageUnit() == dosage2.dosageUnit()
            val frequencyMatch = (dosage1.frequency()!!).compareTo(dosage2.frequency()!!) == 0
            val frequencyUnitMatch = dosage1.frequencyUnit() == dosage2.frequencyUnit()
            val ifNeededMatch = dosage1.ifNeeded() == dosage2.ifNeeded()
            dosageMinMatch && dosageMaxMatch && dosageUnitMatch && frequencyMatch && frequencyUnitMatch && ifNeededMatch
        } else {
            false
        }
    }

    private fun hasDosing(dosage: Dosage): Boolean {
        return dosage.dosageMin() != 0.0 && dosage.dosageMax() != 0.0 && dosage.dosageUnit() != null && dosage.frequency() != null && dosage.frequencyUnit() != null && dosage.ifNeeded() != null
    }
}