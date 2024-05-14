package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Assert.assertTrue
import org.junit.Test

class HasMeasurableDiseaseTest {
    @Test
    fun shouldPassWhenHasMeasurableDiseaseIsTrue() {
        val evaluation = FUNCTION.evaluate(TestTumorFactory.withMeasurableDisease(true))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertTrue(evaluation.recoverable)
    }

    @Test
    fun shouldFailWhenHasMeasurableDiseaseIsFalse() {
        val evaluation = FUNCTION.evaluate(TestTumorFactory.withMeasurableDisease(false))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertTrue(evaluation.recoverable)
    }

    @Test
    fun shouldBeUndeterminedWhenHasMeasurableDiseaseIsUndetermined() {
        val evaluation = FUNCTION.evaluate(TestTumorFactory.withMeasurableDisease(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertTrue(evaluation.recoverable)
    }

    companion object {
        private val FUNCTION = HasMeasurableDisease()
    }
}