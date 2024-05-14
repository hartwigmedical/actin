package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasKnownBrainMetastasesTest {
    @Test
    fun canEvaluate() {
        val function = HasKnownBrainMetastases()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestTumorFactory.withBrainLesions(true)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestTumorFactory.withBrainLesions(false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestTumorFactory.withBrainLesions(null)))
    }
}