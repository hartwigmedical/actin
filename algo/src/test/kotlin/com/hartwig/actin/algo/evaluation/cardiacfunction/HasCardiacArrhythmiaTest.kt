package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasCardiacArrhythmiaTest {
    @Test
    fun canEvaluate() {
        val function = HasCardiacArrhythmia()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(CardiacFunctionTestFactory.withECG(null)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(CardiacFunctionTestFactory.withHasSignificantECGAberration(false)))
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(CardiacFunctionTestFactory.withHasSignificantECGAberration(true, "with description"))
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(CardiacFunctionTestFactory.withHasSignificantECGAberration(true, null)))
    }
}