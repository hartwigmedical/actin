package com.hartwig.actin.algo.evaluation.medication;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class CurrentlyGetsStableMedicationOfCategoryTest {

    @Test
    public void canEvaluateOnOneTerm() {
        String category1 = "category 1";
        CurrentlyGetsStableMedicationOfCategory function =
                new CurrentlyGetsStableMedicationOfCategory(MedicationTestFactory.alwaysActive(), Sets.newHashSet(category1));

        // Fails on no medication
        List<Medication> medications = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // Passes with single medication with dosing.
        medications.add(TestMedicationFactory.builder().from(fixedDosing()).addCategories(category1).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // Passes with another medication with no category and same dosing
        medications.add(TestMedicationFactory.builder().from(fixedDosing()).categories(Sets.newHashSet()).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // Fails on same category and other dosing.
        medications.add(TestMedicationFactory.builder().from(fixedDosing()).addCategories(category1).frequencyUnit("other").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // Also fail in case a dosing is combined with medication without dosing.
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(MedicationTestFactory.withMedications(Lists.newArrayList(TestMedicationFactory.builder()
                        .from(fixedDosing())
                        .addCategories(category1)
                        .build(), TestMedicationFactory.builder().addCategories(category1).build()))));
    }

    @Test
    public void canEvaluateForMultipleTerms() {
        String category1 = "category 1";
        String category2 = "category 2";
        CurrentlyGetsStableMedicationOfCategory function =
                new CurrentlyGetsStableMedicationOfCategory(MedicationTestFactory.alwaysActive(), Sets.newHashSet(category1, category2));

        // Passes with single medication with dosing.
        List<Medication> medications = Lists.newArrayList();
        medications.add(TestMedicationFactory.builder().from(fixedDosing()).addCategories(category1).build());
        medications.add(TestMedicationFactory.builder().from(fixedDosing()).addCategories(category2).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // Passes on same category and other dosing.
        medications.add(TestMedicationFactory.builder().from(fixedDosing()).addCategories(category1).frequencyUnit("other").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // Start failing when both categories have wrong dosing.
        medications.add(TestMedicationFactory.builder().from(fixedDosing()).addCategories(category2).frequencyUnit("other").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)));
    }

    @NotNull
    private static Medication fixedDosing() {
        return TestMedicationFactory.builder()
                .dosageMin(1D)
                .dosageMax(2D)
                .dosageUnit("unit 1")
                .frequency(3D)
                .frequencyUnit("unit 2")
                .ifNeeded(false)
                .build();
    }
}