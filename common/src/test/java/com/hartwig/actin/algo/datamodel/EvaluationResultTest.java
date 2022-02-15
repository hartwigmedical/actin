package com.hartwig.actin.algo.datamodel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EvaluationResultTest {

    @Test
    public void canEvaluateEvaluations() {
        assertTrue(EvaluationResult.PASS.isPass());
        assertTrue(EvaluationResult.PASS_BUT_WARN.isPass());
        assertTrue(EvaluationResult.NOT_EVALUATED.isPass());
        assertFalse(EvaluationResult.FAIL.isPass());
        assertFalse(EvaluationResult.UNDETERMINED.isPass());
        assertFalse(EvaluationResult.NOT_IMPLEMENTED.isPass());
    }

    @Test
    public void canCompareEvaluationResults() {
        assertTrue(EvaluationResult.FAIL.isWorseThan(EvaluationResult.PASS));
        assertTrue(EvaluationResult.UNDETERMINED.isWorseThan(EvaluationResult.PASS_BUT_WARN));
        assertFalse(EvaluationResult.NOT_EVALUATED.isWorseThan(EvaluationResult.NOT_IMPLEMENTED));
    }

    @Test
    public void noEvaluationResultIsWorseThanItself() {
        for (EvaluationResult result : EvaluationResult.values()) {
            assertFalse(result.isWorseThan(result));
        }
    }
}