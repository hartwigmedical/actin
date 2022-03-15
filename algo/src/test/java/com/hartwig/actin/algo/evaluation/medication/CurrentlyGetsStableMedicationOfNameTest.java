package com.hartwig.actin.algo.evaluation.medication;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
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
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // Passes with single medication with dosing.
        medications.add(MedicationTestFactory.active().from(fixedDosing()).name(term1).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // Passes with another medication with other name and other dosing
        medications.add(MedicationTestFactory.builder().from(fixedDosing()).name("other").frequencyUnit("other").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // Fails on same name and other dosing.
        medications.add(MedicationTestFactory.builder().from(fixedDosing()).name(term1).frequencyUnit("other").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // Also fail in case a dosing is combined with medication without dosing.
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(MedicationTestFactory.withMedications(Lists.newArrayList(MedicationTestFactory.active()
                        .from(fixedDosing())
                        .name(term1)
                        .build(), MedicationTestFactory.active().name(term1).build()))));
    }

    @Test
    public void canEvaluateForMultipleTerms() {
        String term1 = "term 1";
        String term2 = "term 2";
        CurrentlyGetsStableMedicationOfName function = new CurrentlyGetsStableMedicationOfName(Sets.newHashSet(term1, term2));

        List<Medication> medications = Lists.newArrayList();
        medications.add(MedicationTestFactory.active().from(fixedDosing()).name(term1).build());
        medications.add(MedicationTestFactory.active().from(fixedDosing()).frequencyUnit("other 1").name(term2).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // Still succeeds when one has different dosing
        medications.add(MedicationTestFactory.builder().from(fixedDosing()).frequencyUnit("other 2").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)));
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