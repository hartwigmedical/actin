package com.hartwig.actin.algo.evaluation.medication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.junit.Test;

public class CurrentlyGetsMedicationOfNameTest {

    @Test
    public void canEvaluate() {
        CurrentlyGetsMedicationOfName function = new CurrentlyGetsMedicationOfName(Sets.newHashSet("term 1"));

        // No medications yet
        List<Medication> medications = Lists.newArrayList();
        assertFalse(function.isPass(MedicationTestFactory.withMedications(medications)));

        // Medication with wrong name
        medications.add(MedicationTestFactory.active().name("This is Term 2").build());
        assertFalse(function.isPass(MedicationTestFactory.withMedications(medications)));

        // Medication with right name
        medications.add(MedicationTestFactory.active().name("This is Term 1").build());
        assertTrue(function.isPass(MedicationTestFactory.withMedications(medications)));

        assertNotNull(function.passMessage());
        assertNotNull(function.failMessage());
    }
}