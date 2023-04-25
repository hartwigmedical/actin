package com.hartwig.actin.algo.evaluation.medication;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory;

import org.junit.Test;

public class HasRecentlyReceivedMedicationOfApproximateCategoryTest {

    private static final LocalDate EVALUATION_DATE =
            TestClinicalFactory.createMinimalTestClinicalRecord().patient().registrationDate().plusWeeks(1);

    @Test
    public void canEvaluateForActiveMedications() {
        HasRecentlyReceivedMedicationOfApproximateCategory function = new HasRecentlyReceivedMedicationOfApproximateCategory(
                MedicationTestFactory.alwaysActive(),
                "category to find",
                EVALUATION_DATE.plusDays(1));

        // No medications
        List<Medication> medications = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // Wrong category
        medications.add(TestMedicationFactory.builder().addCategories("wrong category").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // Right category
        medications.add(TestMedicationFactory.builder().addCategories("category to find").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)));
    }

    @Test
    public void canEvaluateForStoppedMedication() {
        HasRecentlyReceivedMedicationOfApproximateCategory function = new HasRecentlyReceivedMedicationOfApproximateCategory(
                MedicationTestFactory.alwaysStopped(),
                "category to find",
                EVALUATION_DATE.minusDays(1));

        // Medication stopped after min stop date
        Medication medication = TestMedicationFactory.builder().addCategories("category to find").stopDate(EVALUATION_DATE).build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(Lists.newArrayList(medication))));
    }

    @Test
    public void cantDetermineWithOldEvaluationDate() {
        HasRecentlyReceivedMedicationOfApproximateCategory function = new HasRecentlyReceivedMedicationOfApproximateCategory(
                MedicationTestFactory.alwaysStopped(),
                "category to find",
                EVALUATION_DATE.minusWeeks(2));

        // Medication stopped after min stop date
        Medication medication = TestMedicationFactory.builder().addCategories("category to find").stopDate(EVALUATION_DATE).build();
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(MedicationTestFactory.withMedications(Lists.newArrayList(medication))));
    }
}