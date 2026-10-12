package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.general.GeneralTestFactory.withWHO
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HasMaximumWHOStatusTest {

    private val function: HasMaximumWHOStatus = HasMaximumWHOStatus(2)

    @Test
    fun `Should be undetermined when WHO is null`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withWHO(null)),
            "Undetermined if WHO status is within requested max WHO 2 (WHO missing)"
        )
    }

    @Test
    fun `Should pass when WHO is less than or equal to maximum`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withWHO(0)), "WHO 0 is below requested max WHO 2")
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withWHO(1)), "WHO 1 is below requested max WHO 2")
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withWHO(2)), "WHO 2 is below requested max WHO 2")
    }

    @Test
    fun `Should return recoverable fail when WHO difference is exactly one`() {
        val evaluation = function.evaluate(withWHO(3))
        assertEvaluation(EvaluationResult.FAIL, evaluation, "WHO 3 should be below requested max WHO 2")
        assertThat(evaluation.recoverable).isTrue()
    }

    @Test
    fun `Should fail when WHO difference is greater than one`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withWHO(4)), "WHO 4 is not below requested max WHO 2")
    }

    @Test
    fun `Should be undetermined when precision is at most and WHO above maximum`() {
        val evaluation = function.evaluate(withWHO(3, WhoStatusPrecision.AT_MOST))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation, "Undetermined if patient WHO <=3 is below requested max WHO 2")
    }

    @Test
    fun `Should pass when precision is at most and WHO below maximum`() {
        val evaluation = function.evaluate(withWHO(1, WhoStatusPrecision.AT_MOST))
        assertEvaluation(EvaluationResult.PASS, evaluation, "WHO <=1 is below requested max WHO 2")
    }

    @Test
    fun `Should fail when precision is at least and WHO above maximum`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withWHO(4, WhoStatusPrecision.AT_LEAST)),
            "WHO >=4 is not below requested max WHO 2"
        )
    }

    @Test
    fun `Should be undetermined when precision is at least and WHO below maximum`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withWHO(0, WhoStatusPrecision.AT_LEAST)),
            "Undetermined if patient WHO >=0 is below requested max WHO 2"
        )
    }
}
