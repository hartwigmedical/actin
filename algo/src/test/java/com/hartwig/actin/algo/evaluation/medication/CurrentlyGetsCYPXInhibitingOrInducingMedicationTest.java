package com.hartwig.actin.algo.evaluation.medication;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class CurrentlyGetsCYPXInhibitingOrInducingMedicationTest {

    @Test
    public void canEvaluate() {
        CurrentlyGetsCYPXInhibitingOrInducingMedication function = new CurrentlyGetsCYPXInhibitingOrInducingMedication("CYP9A9");

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}