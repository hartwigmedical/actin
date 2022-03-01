package com.hartwig.actin.algo.evaluation.laboratory;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasLimitedPTTTest {

    @Test
    public void canEvaluate() {
        HasLimitedPTT function = new HasLimitedPTT();

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}