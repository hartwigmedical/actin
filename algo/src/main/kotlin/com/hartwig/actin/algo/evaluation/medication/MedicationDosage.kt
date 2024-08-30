package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.datamodel.clinical.Dosage

internal object MedicationDosage {

    fun hasMatchingDosing(dosage1: Dosage, dosage2: Dosage): Boolean {
        return listOf(Dosage::dosageMin, Dosage::dosageMax, Dosage::dosageUnit, Dosage::frequency, Dosage::frequencyUnit, Dosage::ifNeeded)
            .map {
                val val1 = it.invoke(dosage1)
                val val2 = it.invoke(dosage2)
                val1 != null && val2 != null && val1 == val2
            }
            .reduce(Boolean::and)
    }
}