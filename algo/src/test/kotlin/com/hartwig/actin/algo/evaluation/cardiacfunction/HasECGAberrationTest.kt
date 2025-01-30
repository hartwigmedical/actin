package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasECGAberrationTest {
    @Test
    fun canEvaluate() {
        val function = HasEcgAberration()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(CardiacFunctionTestFactory.withEcg(null)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(CardiacFunctionTestFactory.withHasSignificantEcgAberration(false)))
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(CardiacFunctionTestFactory.withHasSignificantEcgAberration(true, "with description"))
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(CardiacFunctionTestFactory.withHasSignificantEcgAberration(true, null)))
    }
}