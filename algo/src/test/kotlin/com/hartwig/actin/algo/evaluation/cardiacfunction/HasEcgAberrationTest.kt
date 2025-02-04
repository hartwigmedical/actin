package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasEcgAberrationTest {
    private val function = HasEcgAberration()

    @Test
    fun `Should fail with no ECG aberration`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(CardiacFunctionTestFactory.withEcg(null)))
    }

    @Test
    fun `Should pass with ECG aberration`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(CardiacFunctionTestFactory.withEcgDescription("with description"))
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(CardiacFunctionTestFactory.withEcgDescription(null)))
    }
}