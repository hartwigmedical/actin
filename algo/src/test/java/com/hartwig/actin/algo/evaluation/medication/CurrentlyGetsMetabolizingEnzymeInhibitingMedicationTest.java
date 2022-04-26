package com.hartwig.actin.algo.evaluation.medication;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.junit.Test;

public class CurrentlyGetsMetabolizingEnzymeInhibitingMedicationTest {

    private static final LocalDate EVALUATION_DATE = LocalDate.of(2020, 4, 20);

    @Test
    public void canEvaluate() {
        CurrentlyGetsMetabolizingEnzymeInhibitingMedication function =
                new CurrentlyGetsMetabolizingEnzymeInhibitingMedication(EVALUATION_DATE);

        // No medications yet
        List<Medication> medications = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // Various inactive medications
        medications.add(MedicationTestFactory.inactive(EVALUATION_DATE).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // One active medication
        medications.add(MedicationTestFactory.active(EVALUATION_DATE).build());
        assertEvaluation(EvaluationResult.WARN, function.evaluate(MedicationTestFactory.withMedications(medications)));
    }
}
