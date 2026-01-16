package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasMaximumWHOStatusTest {

    private val function: HasMaximumWHOStatus = HasMaximumWHOStatus(2)

    @Test
    fun `Should return undetermined when WHO is null`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(GeneralTestFactory.withWHO(null)))
    }

    @Test
    fun `Should pass when WHO is less than or equal to maximum`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withWHO(0)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withWHO(1)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withWHO(2)))
    }

    @Test
    fun `Should return recoverable fail when WHO difference is exactly one`() {
        val evaluation = function.evaluate(GeneralTestFactory.withWHO(3))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.recoverable).isTrue()
    }

    @Test
    fun `Should return recoverable fail when WHO difference is exactly one with at most range`() {
        val evaluation = function.evaluate(GeneralTestFactory.withWHO(3, WhoStatusPrecision.AT_MOST))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.recoverable).isTrue()
        assertThat(evaluation.failMessages.first().toString()).isEqualTo("WHO <=3 exceeds WHO 2")
    }

    @Test
    fun `Should fail when WHO difference is greater than one`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withWHO(4)))
    }

    @Test
    fun `Should return undetermined when precision is at least`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(GeneralTestFactory.withWHO(4, WhoStatusPrecision.AT_LEAST))
        )
    }

    @Test
    fun `Should pass when WHO at most value is less than or equal to maximum`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(GeneralTestFactory.withWHO(2, WhoStatusPrecision.AT_MOST))
        )
    }

    @Test
    fun `Should fail when WHO at most value is more than the maximum`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(GeneralTestFactory.withWHO(3, WhoStatusPrecision.AT_MOST))
        )
    }

}