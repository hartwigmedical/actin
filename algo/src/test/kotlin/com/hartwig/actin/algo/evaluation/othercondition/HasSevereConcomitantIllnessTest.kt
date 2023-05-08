package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasSevereConcomitantIllnessTest {
    @Test
    fun canEvaluate() {
        val function = HasSevereConcomitantIllness()
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(OtherConditionTestFactory.withWHO(0)))
        assertEvaluation(EvaluationResult.WARN, function.evaluate(OtherConditionTestFactory.withWHO(4)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withWHO(5)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(OtherConditionTestFactory.withWHO(null)))
    }
}