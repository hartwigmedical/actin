package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasNormalCardiacFunctionByMugaOrTteTest {
    private val function = HasNormalCardiacFunctionByMugaOrTte()

    @Test
    fun `Should be undetermined for unknown LVEF`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(CardiacFunctionTestFactory.withLvef(null)))
    }

    @Test
    fun `Should be undetermined for LVEF greater than or equal to 50 percent`() {
        listOf(0.5, 0.8).forEach { lvef ->
            assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(CardiacFunctionTestFactory.withLvef(lvef)))
        }
    }

    @Test
    fun `Should warn for LVEF under 50 percent`() {
        assertEvaluation(EvaluationResult.WARN, function.evaluate(CardiacFunctionTestFactory.withLvef(0.3)))
    }
}