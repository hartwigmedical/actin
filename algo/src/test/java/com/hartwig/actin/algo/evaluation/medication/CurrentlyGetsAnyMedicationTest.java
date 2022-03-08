package com.hartwig.actin.algo.evaluation.medication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.junit.Test;

public class CurrentlyGetsAnyMedicationTest {

    @Test
    public void canEvaluate() {
        CurrentlyGetsAnyMedication function = new CurrentlyGetsAnyMedication();

        // No medications yet
        List<Medication> medications = Lists.newArrayList();
        assertFalse(function.isPass(MedicationTestFactory.withMedications(medications)));

        // Various inactive medications
        medications.add(MedicationTestFactory.builder().active(false).build());
        medications.add(MedicationTestFactory.builder().active(null).build());
        assertFalse(function.isPass(MedicationTestFactory.withMedications(medications)));

        // One active medication
        medications.add(MedicationTestFactory.builder().active(true).build());
        assertTrue(function.isPass(MedicationTestFactory.withMedications(medications)));

        assertNotNull(function.passMessage());
        assertNotNull(function.failMessage());
    }
}