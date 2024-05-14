package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Assert.assertTrue
import org.junit.Test

class HasEvaluableDiseaseTest {
    @Test
    fun `Should pass when has measurable disease is true`() {
        val evaluation = FUNCTION.evaluate(TestTumorFactory.withMeasurableDisease(true))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertTrue(evaluation.recoverable)
    }

    @Test
    fun `Should be undetermined when has measurable disease is false`() {
        val evaluation = FUNCTION.evaluate(TestTumorFactory.withMeasurableDisease(false))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertTrue(evaluation.recoverable)
    }

    @Test
    fun `Should be undetermined when has measurable disease is unknown`() {
        val evaluation = FUNCTION.evaluate(TestTumorFactory.withMeasurableDisease(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertTrue(evaluation.recoverable)
    }

    companion object {
        private val FUNCTION = HasEvaluableDisease()
    }
}