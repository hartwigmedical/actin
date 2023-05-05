package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.medication.MedicationDosage.hasMatchingDosing
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationDosageTest {
    @Test
    fun canAssessDosingStability() {
        val dosing1: Medication = TestMedicationFactory.builder()
            .dosageMin(1.0)
            .dosageMax(2.0)
            .dosageUnit("unit 1")
            .frequency(3.0)
            .frequencyUnit("unit 2")
            .ifNeeded(false)
            .build()
        val dosing2: Medication = TestMedicationFactory.builder()
            .dosageMin(2.0)
            .dosageMax(3.0)
            .dosageUnit("unit 2")
            .frequency(4.0)
            .frequencyUnit("unit 1")
            .ifNeeded(false)
            .build()
        assertTrue(hasMatchingDosing(dosing1, dosing1))
        assertTrue(hasMatchingDosing(dosing2, dosing2))
        assertFalse(hasMatchingDosing(dosing1, dosing2))
        assertFalse(hasMatchingDosing(dosing1, TestMedicationFactory.builder().build()))
    }
}