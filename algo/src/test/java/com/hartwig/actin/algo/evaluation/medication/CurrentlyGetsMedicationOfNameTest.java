package com.hartwig.actin.algo.evaluation.medication;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.junit.Test;

public class CurrentlyGetsMedicationOfNameTest {

    private static final LocalDate EVALUATION_DATE = LocalDate.of(2020, 4, 20);

    @Test
    public void canEvaluate() {
        CurrentlyGetsMedicationOfName function = new CurrentlyGetsMedicationOfName(EVALUATION_DATE, Sets.newHashSet("term 1"));

        // No medications yet
        List<Medication> medications = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // Medication with wrong name
        medications.add(MedicationTestFactory.active(EVALUATION_DATE).name("This is Term 2").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // Medication with right name
        medications.add(MedicationTestFactory.active(EVALUATION_DATE).name("This is Term 1").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)));
    }
}