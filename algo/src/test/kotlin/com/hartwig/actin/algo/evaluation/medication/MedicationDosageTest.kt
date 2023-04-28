package com.hartwig.actin.algo.evaluation.medication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory;

import org.junit.Test;

public class MedicationDosageTest {

    @Test
    public void canAssessDosingStability() {
        Medication dosing1 = TestMedicationFactory.builder()
                .dosageMin(1D)
                .dosageMax(2D)
                .dosageUnit("unit 1")
                .frequency(3D)
                .frequencyUnit("unit 2")
                .ifNeeded(false)
                .build();

        Medication dosing2 = TestMedicationFactory.builder()
                .dosageMin(2D)
                .dosageMax(3D)
                .dosageUnit("unit 2")
                .frequency(4D)
                .frequencyUnit("unit 1")
                .ifNeeded(false)
                .build();

        assertTrue(MedicationDosage.hasMatchingDosing(dosing1, dosing1));
        assertTrue(MedicationDosage.hasMatchingDosing(dosing2, dosing2));
        assertFalse(MedicationDosage.hasMatchingDosing(dosing1, dosing2));
        assertFalse(MedicationDosage.hasMatchingDosing(dosing1, TestMedicationFactory.builder().build()));
    }

}