package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasKnownBrainMetastasesTest {
    @Test
    fun canEvaluate() {
        val function = HasKnownBrainMetastases()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBrainLesions(true)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBrainLesions(false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBrainLesions(null)))
    }
}