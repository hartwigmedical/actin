package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HasEvaluableDiseaseTest {

    private val function = HasEvaluableDisease()

    @Test
    fun `Should pass when has measurable disease is true`() {
        val evaluation = function.evaluate(TumorTestFactory.withMeasurableDisease(true))
        assertEvaluation(EvaluationResult.PASS, evaluation, "Disease is evaluable (because known measurable disease)")
        assertThat(evaluation.recoverable).isTrue()
    }

    @Test
    fun `Should be undetermined when has measurable disease is false`() {
        val evaluation = function.evaluate(TumorTestFactory.withMeasurableDisease(false))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation, "Undetermined if disease may be evaluable")
        assertThat(evaluation.recoverable).isTrue()
    }

    @Test
    fun `Should be undetermined when has measurable disease is unknown`() {
        val evaluation = function.evaluate(TumorTestFactory.withMeasurableDisease(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation, "Undetermined if disease may be evaluable")
        assertThat(evaluation.recoverable).isTrue()
    }
}