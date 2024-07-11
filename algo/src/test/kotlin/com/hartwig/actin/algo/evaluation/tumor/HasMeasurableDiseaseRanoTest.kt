package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Assert.assertTrue
import org.junit.Test

class HasMeasurableDiseaseRanoTest {

    private val doidModel = TestDoidModelFactory.createWithOneParentChild(DoidConstants.CNS_CANCER_DOID, DoidConstants.BRAIN_CANCER_DOID)
    private val function = HasMeasurableDiseaseRano(doidModel)

    @Test
    fun `Should pass when has measurable disease is true and brain cancer`() {
        val evaluation = function.evaluate(TumorTestFactory.withMeasurableDiseaseAndDoid(true, DoidConstants.BRAIN_CANCER_DOID))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertTrue(evaluation.recoverable)
    }

    @Test
    fun `Should fail when has measurable disease is false`() {
        val evaluation = function.evaluate(TumorTestFactory.withMeasurableDisease(false))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertTrue(evaluation.recoverable)
    }

    @Test
    fun `Should be undetermined when has measurable disease is undetermined`() {
        val evaluation = function.evaluate(TumorTestFactory.withMeasurableDisease(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertTrue(evaluation.recoverable)
    }

    @Test
    fun `Should warn when has measurable disease is true but has colorectal cancer`() {
        val evaluation = function.evaluate(
            TumorTestFactory.withMeasurableDiseaseAndDoid(
                true,
                DoidConstants.COLORECTAL_CANCER_DOID
            )
        )
        assertEvaluation(EvaluationResult.WARN, evaluation)
    }
}