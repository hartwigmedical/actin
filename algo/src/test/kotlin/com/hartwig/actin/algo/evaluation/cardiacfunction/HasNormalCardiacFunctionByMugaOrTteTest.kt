package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasNormalCardiacFunctionByMugaOrTteTest {

    @Test
    fun canEvaluate() {
        val function = HasNormalCardiacFunctionByMugaOrTte()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(CardiacFunctionTestFactory.withLvef(null)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(CardiacFunctionTestFactory.withLvef(0.8)))
        assertEvaluation(EvaluationResult.WARN, function.evaluate(CardiacFunctionTestFactory.withLvef(0.3)))
    }
}