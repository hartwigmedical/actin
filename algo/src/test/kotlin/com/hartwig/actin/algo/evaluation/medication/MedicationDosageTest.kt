package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.medication.MedicationDosage.hasMatchingDosing
import com.hartwig.actin.clinical.datamodel.Dosage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationDosageTest {
    @Test
    fun canAssessDosingStability() {
        val dosing1 = Dosage(
            dosageMin = 1.0,
            dosageMax = 2.0,
            dosageUnit = "unit 1",
            frequency = 3.0,
            frequencyUnit = "unit 2",
            ifNeeded = false
        )
        val dosing2 = Dosage(
            dosageMin = 2.0,
            dosageMax = 3.0,
            dosageUnit = "unit 2",
            frequency = 4.0,
            frequencyUnit = "unit 1",
            ifNeeded = false
        )
        assertTrue(hasMatchingDosing(dosing1, dosing1))
        assertTrue(hasMatchingDosing(dosing2, dosing2))
        assertFalse(hasMatchingDosing(dosing1, dosing2))
        assertFalse(hasMatchingDosing(dosing1, Dosage()))
    }
}