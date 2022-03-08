package com.hartwig.actin.algo.evaluation.medication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.junit.Test;

public class CurrentlyGetsMedicationOfApproximateCategoryTest {

    @Test
    public void canEvaluate() {
        CurrentlyGetsMedicationOfApproximateCategory function = new CurrentlyGetsMedicationOfApproximateCategory("category 1");

        // No medications yet
        List<Medication> medications = Lists.newArrayList();
        assertFalse(function.isPass(MedicationTestFactory.withMedications(medications)));

        // Medication with wrong category
        medications.add(MedicationTestFactory.active().addCategories("category 2", "category 3").build());
        assertFalse(function.isPass(MedicationTestFactory.withMedications(medications)));

        // Medication with non-exact category
        medications.add(MedicationTestFactory.active().addCategories("this is category 1").build());
        assertTrue(function.isPass(MedicationTestFactory.withMedications(medications)));

        // Medication with right category
        assertTrue(function.isPass(MedicationTestFactory.withMedications(Lists.newArrayList(MedicationTestFactory.active()
                .addCategories("category 4", "category 1")
                .build()))));

        assertNotNull(function.passMessage());
        assertNotNull(function.failMessage());
    }
}