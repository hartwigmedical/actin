package com.hartwig.actin.algo.datamodel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EvaluationTest {

    @Test
    public void canEvaluateEvaluations() {
        assertTrue(Evaluation.PASS.isPass());
        assertTrue(Evaluation.PASS_BUT_WARN.isPass());
        assertTrue(Evaluation.IGNORED.isPass());
        assertFalse(Evaluation.FAIL.isPass());
        assertFalse(Evaluation.UNDETERMINED.isPass());
        assertFalse(Evaluation.NOT_IMPLEMENTED.isPass());
    }
}