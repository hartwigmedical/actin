package com.hartwig.actin.algo.datamodel

import org.junit.Assert
import org.junit.Test

class EvaluationResultTest {
    @Test
    fun canCompareEvaluationResults() {
        Assert.assertTrue(EvaluationResult.FAIL.isWorseThan(EvaluationResult.PASS))
        Assert.assertTrue(EvaluationResult.WARN.isWorseThan(EvaluationResult.UNDETERMINED))
        Assert.assertFalse(EvaluationResult.NOT_EVALUATED.isWorseThan(EvaluationResult.NOT_IMPLEMENTED))
    }

    @Test
    fun noEvaluationResultIsWorseThanItself() {
        for (result in EvaluationResult.values()) {
            Assert.assertFalse(result.isWorseThan(result))
        }
    }
}