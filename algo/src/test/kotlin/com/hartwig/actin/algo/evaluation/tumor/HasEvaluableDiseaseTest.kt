package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasEvaluableDiseaseTest {

    private val function = HasEvaluableDisease()

    @Test
    fun `Should pass when has measurable disease is true`() {
        val evaluation = function.evaluate(TumorTestFactory.withMeasurableDisease(true))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.recoverable).isTrue()
    }

    @Test
    fun `Should be undetermined when has measurable disease is false`() {
        val evaluation = function.evaluate(TumorTestFactory.withMeasurableDisease(false))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.recoverable).isTrue()
    }

    @Test
    fun `Should be undetermined when has measurable disease is unknown`() {
        val evaluation = function.evaluate(TumorTestFactory.withMeasurableDisease(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.recoverable).isTrue()
    }
}