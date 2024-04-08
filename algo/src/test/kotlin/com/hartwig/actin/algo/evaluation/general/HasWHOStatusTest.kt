package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasWHOStatusTest {

    private val function = HasWHOStatus(2)

    @Test
    fun `Should return undetermined when WHO is null`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(GeneralTestFactory.withWHO(null)))
    }

    @Test
    fun `Should fail when WHO difference is greater than one`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withWHO(0)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withWHO(4)))
    }

    @Test
    fun `Should return recoverable fail when WHO difference is exactly one`() {
        val evaluationFor1 = function.evaluate(GeneralTestFactory.withWHO(1))
        assertEvaluation(EvaluationResult.FAIL, evaluationFor1)
        assertThat(evaluationFor1.recoverable).isTrue

        val evaluationFor3 = function.evaluate(GeneralTestFactory.withWHO(3))
        assertEvaluation(EvaluationResult.FAIL, evaluationFor3)
        assertThat(evaluationFor3.recoverable).isTrue
    }

    @Test
    fun `Should pass when WHO is exact match`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withWHO(2)))
    }
}