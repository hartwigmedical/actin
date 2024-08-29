package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Assert.assertTrue
import org.junit.Test

class HasMeasurableDiseaseTest {
    @Test
    fun shouldPassWhenHasMeasurableDiseaseIsTrue() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withMeasurableDisease(true))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertTrue(evaluation.recoverable)
    }

    @Test
    fun shouldFailWhenHasMeasurableDiseaseIsFalse() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withMeasurableDisease(false))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertTrue(evaluation.recoverable)
    }

    @Test
    fun shouldBeUndeterminedWhenHasMeasurableDiseaseIsUndetermined() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withMeasurableDisease(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertTrue(evaluation.recoverable)
    }

    companion object {
        private val FUNCTION = HasMeasurableDisease()
    }
}