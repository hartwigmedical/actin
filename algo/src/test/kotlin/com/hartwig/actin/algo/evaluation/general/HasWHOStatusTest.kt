package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
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
    fun `Should warn when WHO difference is exactly one`() {
        val evaluationFor1 = function.evaluate(GeneralTestFactory.withWHO(1))
        assertEvaluation(EvaluationResult.WARN, evaluationFor1)

        val evaluationFor3 = function.evaluate(GeneralTestFactory.withWHO(3))
        assertEvaluation(EvaluationResult.WARN, evaluationFor3)
    }

    @Test
    fun `Should pass when WHO is exact match`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withWHO(2)))
    }
}