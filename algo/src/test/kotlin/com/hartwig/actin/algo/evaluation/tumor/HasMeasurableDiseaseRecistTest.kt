package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasMeasurableDiseaseRecistTest {

    private val doidModel = TestDoidModelFactory.createWithOneParentChild("100", "200")
    private val function = HasMeasurableDiseaseRecist(doidModel)

    @Test
    fun `Should pass when has measurable disease is true`() {
        val evaluation = function.evaluate(TumorTestFactory.withMeasurableDisease(true))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.recoverable).isTrue()
    }

    @Test
    fun `Should pass when has measurable disease is true and arbitrary doid`() {
        val evaluation = function.evaluate(TumorTestFactory.withMeasurableDiseaseAndDoid(true, "arbitrary"))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.recoverable).isTrue()
    }

    @Test
    fun `Should fail when measurable disease is false`() {
        val evaluation = function.evaluate(TumorTestFactory.withMeasurableDisease(false))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.recoverable).isTrue()
    }

    @Test
    fun `Should be undetermined when has measurable disease is undetermined`() {
        val evaluation = function.evaluate(TumorTestFactory.withMeasurableDisease(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.recoverable).isTrue()
    }

    @Test
    fun `Should warn when uncertain if evaluated against RECIST`() {
        val evaluation = function.evaluate(
            TumorTestFactory.withMeasurableDiseaseAndDoid(
                true,
                HasMeasurableDiseaseRecist.NON_RECIST_TUMOR_DOIDS.iterator().next()
            )
        )
        assertEvaluation(EvaluationResult.WARN, evaluation)
    }
}