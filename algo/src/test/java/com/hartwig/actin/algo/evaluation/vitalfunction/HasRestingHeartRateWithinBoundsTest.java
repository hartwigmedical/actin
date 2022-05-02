package com.hartwig.actin.algo.evaluation.vitalfunction;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasRestingHeartRateWithinBoundsTest {

    @Test
    public void canEvaluate() {
        HasRestingHeartRateWithinBounds function = new HasRestingHeartRateWithinBounds(0, 0);

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}