package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasECGAberrationTest {
    @Test
    fun canEvaluate() {
        val function = HasECGAberration()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(CardiacFunctionTestFactory.withECG(null)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(CardiacFunctionTestFactory.withHasSignificantECGAberration(false)))
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(CardiacFunctionTestFactory.withHasSignificantECGAberration(true, "with description"))
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(CardiacFunctionTestFactory.withHasSignificantECGAberration(true, null)))
    }
}