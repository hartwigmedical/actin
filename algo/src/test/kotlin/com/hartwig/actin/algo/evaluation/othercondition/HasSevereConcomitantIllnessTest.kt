package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasSevereConcomitantIllnessTest {
    val function = HasSevereConcomitantIllness()

    @Test
    fun `Should not evaluate when WHO unknown`() {
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(OtherConditionTestFactory.withWHO(null)))
    }

    @Test
    fun `Should not evaluate when WHO 2 or less`() {
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(OtherConditionTestFactory.withWHO(0)))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(OtherConditionTestFactory.withWHO(1)))
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(OtherConditionTestFactory.withWHO(2)))
    }

    @Test
    fun `Should warn when WHO 3 or 4`() {
        assertEvaluation(EvaluationResult.WARN, function.evaluate(OtherConditionTestFactory.withWHO(4)))
        assertEvaluation(EvaluationResult.WARN, function.evaluate(OtherConditionTestFactory.withWHO(3)))
    }

    @Test
    fun `Should pass when WHO 5`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withWHO(5)))
    }
}