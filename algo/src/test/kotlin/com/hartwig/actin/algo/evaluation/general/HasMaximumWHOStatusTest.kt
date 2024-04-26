package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
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
    fun `Should fail when WHO is greater than maximum`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withWHO(3)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withWHO(4)))
    }
}