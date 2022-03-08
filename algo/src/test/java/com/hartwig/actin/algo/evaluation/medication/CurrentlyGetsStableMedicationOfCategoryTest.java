package com.hartwig.actin.algo.evaluation.medication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.junit.Test;

public class CurrentlyGetsStableMedicationOfCategoryTest {

    @Test
    public void canEvaluate() {
        String category = "category 1";
        CurrentlyGetsStableMedicationOfCategory function = new CurrentlyGetsStableMedicationOfCategory(category);

        // Fails on no medication
        List<Medication> medications = Lists.newArrayList();
        assertFalse(function.isPass(MedicationTestFactory.withMedications(medications)));

        Medication randomDosing = MedicationTestFactory.active()
                .addCategories(category)
                .dosageMin(1D)
                .dosageMax(2D)
                .dosageUnit("unit 1")
                .frequency(3D)
                .frequencyUnit("unit 2")
                .ifNeeded(false)
                .build();

        // Passes with single medication with dosing.
        medications.add(randomDosing);
        assertTrue(function.isPass(MedicationTestFactory.withMedications(medications)));

        // Passes with another medication with no category and same dosing
        medications.add(MedicationTestFactory.builder().from(randomDosing).categories(Sets.newHashSet()).build());
        assertTrue(function.isPass(MedicationTestFactory.withMedications(medications)));

        // Fails on same category and other dosing.
        medications.add(MedicationTestFactory.builder().from(randomDosing).frequencyUnit("some other unit").build());
        assertFalse(function.isPass(MedicationTestFactory.withMedications(medications)));

        // Also fail in case a dosing is combined with medication without dosing.
        assertFalse(function.isPass(MedicationTestFactory.withMedications(Lists.newArrayList(randomDosing,
                MedicationTestFactory.active().addCategories(category).build()))));

        assertNotNull(function.passMessage());
        assertNotNull(function.failMessage());
    }
}