package com.hartwig.actin.algo.datamodel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EvaluationResultTest {

    @Test
    public void canCompareEvaluationResults() {
        assertTrue(EvaluationResult.FAIL.isWorseThan(EvaluationResult.PASS));
        assertTrue(EvaluationResult.WARN.isWorseThan(EvaluationResult.UNDETERMINED));

        assertFalse(EvaluationResult.NOT_EVALUATED.isWorseThan(EvaluationResult.NOT_IMPLEMENTED));
    }

    @Test
    public void noEvaluationResultIsWorseThanItself() {
        for (EvaluationResult result : EvaluationResult.values()) {
            assertFalse(result.isWorseThan(result));
        }
    }
}