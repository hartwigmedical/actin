package com.hartwig.actin.algo.evaluation.medication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class CurrentlyGetsStableMedicationOfNameTest {

    @Test
    public void canEvaluateForOneTerm() {
        String term1 = "term 1";
        CurrentlyGetsStableMedicationOfName function = new CurrentlyGetsStableMedicationOfName(Sets.newHashSet(term1));

        // Fails on no medication
        List<Medication> medications = Lists.newArrayList();
        assertFalse(function.isPass(MedicationTestFactory.withMedications(medications)));

        // Passes with single medication with dosing.
        medications.add(MedicationTestFactory.active().from(fixedDosing()).name(term1).build());
        assertTrue(function.isPass(MedicationTestFactory.withMedications(medications)));

        // Passes with another medication with other name and other dosing
        medications.add(MedicationTestFactory.builder().from(fixedDosing()).name("other").frequencyUnit("other").build());
        assertTrue(function.isPass(MedicationTestFactory.withMedications(medications)));

        // Fails on same name and other dosing.
        medications.add(MedicationTestFactory.builder().from(fixedDosing()).name(term1).frequencyUnit("other").build());
        assertFalse(function.isPass(MedicationTestFactory.withMedications(medications)));

        // Also fail in case a dosing is combined with medication without dosing.
        assertFalse(function.isPass(MedicationTestFactory.withMedications(Lists.newArrayList(MedicationTestFactory.active()
                .from(fixedDosing())
                .name(term1)
                .build(), MedicationTestFactory.active().name(term1).build()))));

        assertNotNull(function.passMessage());
        assertNotNull(function.failMessage());
    }

    @Test
    public void canEvaluateForMultipleTerms() {
        String term1 = "term 1";
        String term2 = "term 2";
        CurrentlyGetsStableMedicationOfName function = new CurrentlyGetsStableMedicationOfName(Sets.newHashSet(term1, term2));

        List<Medication> medications = Lists.newArrayList();
        medications.add(MedicationTestFactory.active().from(fixedDosing()).name(term1).build());
        medications.add(MedicationTestFactory.active().from(fixedDosing()).frequencyUnit("other 1").name(term2).build());
        assertTrue(function.isPass(MedicationTestFactory.withMedications(medications)));

        // Still succeeds when one has different dosing
        medications.add(MedicationTestFactory.builder().from(fixedDosing()).frequencyUnit("other 2").build());
        assertTrue(function.isPass(MedicationTestFactory.withMedications(medications)));

        assertNotNull(function.passMessage());
        assertNotNull(function.failMessage());
    }

    @NotNull
    private static Medication fixedDosing() {
        return MedicationTestFactory.active()
                .dosageMin(1D)
                .dosageMax(2D)
                .dosageUnit("unit 1")
                .frequency(3D)
                .frequencyUnit("unit 2")
                .ifNeeded(false)
                .build();
    }
}