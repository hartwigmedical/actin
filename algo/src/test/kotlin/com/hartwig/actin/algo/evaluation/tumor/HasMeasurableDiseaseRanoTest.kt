package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Assert.assertTrue
import org.junit.Test

class HasMeasurableDiseaseRanoTest {
    @Test
    fun `Should pass when has measurable disease is true`() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withMeasurableDisease(true))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertTrue(evaluation.recoverable)
    }

    @Test
    fun `Should pass when has measurable disease is true and brain cancer`() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withMeasurableDiseaseAndDoid(true, DoidConstants.BRAIN_CANCER_DOID))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertTrue(evaluation.recoverable)
    }

    @Test
    fun `Should fail when has measurable disease is false`() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withMeasurableDisease(false))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertTrue(evaluation.recoverable)
    }

    @Test
    fun `Should be undetermined when has measurable disease is undetermined`() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withMeasurableDisease(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertTrue(evaluation.recoverable)
    }

    @Test
    fun `Should warn when has measurable disease is true but has colorectal cancer`() {
        val evaluation = FUNCTION.evaluate(
            TumorTestFactory.withMeasurableDiseaseAndDoid(
                true,
                DoidConstants.COLORECTAL_CANCER_DOID
            )
        )
        assertEvaluation(EvaluationResult.WARN, evaluation)
    }

    companion object {
        private val DOID_MODEL = TestDoidModelFactory.createWithOneParentChild("100", "200")
        private val FUNCTION = HasMeasurableDiseaseRano(DOID_MODEL)
    }
}