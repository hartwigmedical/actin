package com.hartwig.actin.algo.evaluation.medication;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory;

import org.junit.Test;

public class CurrentlyGetsMedicationOfApproximateCategoryTest {

    @Test
    public void canEvaluate() {
        CurrentlyGetsMedicationOfApproximateCategory function =
                new CurrentlyGetsMedicationOfApproximateCategory(MedicationTestFactory.alwaysActive(), "category 1");

        // No medications yet
        List<Medication> medications = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // Medication with wrong category
        medications.add(TestMedicationFactory.builder().addCategories("category 2", "category 3").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // Medication with non-exact category
        medications.add(TestMedicationFactory.builder().addCategories("this is category 1").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // Medication with right category
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(MedicationTestFactory.withMedications(Lists.newArrayList(TestMedicationFactory.builder()
                        .addCategories("category 4", "category 1")
                        .build()))));
    }
}