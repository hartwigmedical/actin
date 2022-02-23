package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasExhaustedSOCTreatmentsTest {

    @Test
    public void canEvaluate() {
        HasExhaustedSOCTreatments function = new HasExhaustedSOCTreatments();

        assertEvaluation(EvaluationResult.PASS_BUT_WARN, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}