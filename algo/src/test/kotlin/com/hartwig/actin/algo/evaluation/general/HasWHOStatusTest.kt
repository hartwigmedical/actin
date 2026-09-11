package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HasWHOStatusTest {

    private val function = HasWHOStatus(2)

    @Test
    fun `Should be undetermined when WHO is null`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(GeneralTestFactory.withWHO(null)),
            "Undetermined if WHO status is equal to requested WHO 2 (WHO data missing)"
        )
    }

    @Test
    fun `Should fail when WHO difference is greater than one`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(GeneralTestFactory.withWHO(0)),
            "WHO is 0 but should be exactly WHO 2"
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(GeneralTestFactory.withWHO(4)),
            "WHO is 4 but should be exactly WHO 2"
        )
    }

    @Test
    fun `Should be recoverable fail when WHO difference is exactly one`() {
        val evaluationFor1 = function.evaluate(GeneralTestFactory.withWHO(1))
        assertEvaluation(EvaluationResult.FAIL, evaluationFor1, "WHO is 1 but should be exactly WHO 2")
        assertThat(evaluationFor1.recoverable).isTrue()

        val evaluationFor3 = function.evaluate(GeneralTestFactory.withWHO(3))
        assertEvaluation(EvaluationResult.FAIL, evaluationFor3, "WHO is 3 but should be exactly WHO 2")
        assertThat(evaluationFor3.recoverable).isTrue()
    }

    @Test
    fun `Should pass when WHO is exact match`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(GeneralTestFactory.withWHO(2)),
            "WHO 2 is exactly requested WHO 2"
        )
    }

    @Test
    fun `Should be undetermined when WHO is an at least range below requested WHO`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(GeneralTestFactory.withWHO(1, WhoStatusPrecision.AT_LEAST)),
            "Undetermined if WHO >=1 is exactly requested WHO 2"
        )
    }

    @Test
    fun `Should fail when WHO is an at least range above requested WHO`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(GeneralTestFactory.withWHO(3, WhoStatusPrecision.AT_LEAST)),
            "WHO is >=3 but should be exactly WHO 2"
        )
    }

    @Test
    fun `Should be undetermined when WHO is at most range above requested WHO`() {
        val evaluation = function.evaluate(GeneralTestFactory.withWHO(3, WhoStatusPrecision.AT_MOST))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "Undetermined if WHO <=3 is exactly requested WHO 2"
        )
    }

    @Test
    fun `Should fail when WHO is at most range below requested WHO`() {
        val evaluation = function.evaluate(GeneralTestFactory.withWHO(1, WhoStatusPrecision.AT_MOST))
        assertEvaluation(EvaluationResult.FAIL, evaluation, "WHO is <=1 but should be exactly WHO 2")
    }
}
