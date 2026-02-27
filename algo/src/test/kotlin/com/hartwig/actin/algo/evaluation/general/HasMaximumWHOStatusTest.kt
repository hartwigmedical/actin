package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.general.GeneralTestFactory.withWHO
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasMaximumWHOStatusTest {

    private val function: HasMaximumWHOStatus = HasMaximumWHOStatus(2)

    @Test
    fun `Should return undetermined when WHO is null`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withWHO(null)))
    }

    @Test
    fun `Should pass when WHO is less than or equal to maximum`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withWHO(0)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withWHO(1)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withWHO(2)))
    }

    @Test
    fun `Should return recoverable fail when WHO difference is exactly one`() {
        val evaluation = function.evaluate(withWHO(3))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.recoverable).isTrue()
    }

    @Test
    fun `Should return undetermined when WHO is at most and exceeding maximum`() {
        val evaluation = function.evaluate(withWHO(3, WhoStatusPrecision.AT_MOST))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages.first().toString()).isEqualTo("Undetermined if WHO <=3 exceeds WHO 2")
    }

    @Test
    fun `Should fail when WHO difference is greater than one`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withWHO(4)))
    }

    @Test
    fun `Should return undetermined when precision is at least`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(withWHO(4, WhoStatusPrecision.AT_LEAST))
        )
    }

    @Test
    fun `Should pass when WHO at most value is less than or equal to maximum`() {
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(withWHO(2, WhoStatusPrecision.AT_MOST))
        )
    }

    @Test
    fun `Should result in pass when WHO with at most range below maximum`() {
        val evaluation = function.evaluate(withWHO(1, WhoStatusPrecision.AT_MOST))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passMessages.first().toString()).isEqualTo("WHO <=1 is below WHO 2")
    }
}