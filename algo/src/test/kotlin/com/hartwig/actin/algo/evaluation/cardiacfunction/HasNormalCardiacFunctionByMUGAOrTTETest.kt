package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasNormalCardiacFunctionByMUGAOrTTETest {
    @Test
    fun canEvaluate() {
        val function = HasNormalCardiacFunctionByMUGAOrTTE()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(CardiacFunctionTestFactory.withLVEF(null)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(CardiacFunctionTestFactory.withLVEF(0.8)))
        assertEvaluation(EvaluationResult.WARN, function.evaluate(CardiacFunctionTestFactory.withLVEF(0.3)))
    }
}