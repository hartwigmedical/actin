package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasKnownActiveBrainMetastasesTest {
    @Test
    fun canEvaluate() {
        val function = HasKnownActiveBrainMetastases()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestTumorFactory.withBrainLesionStatus(null, null)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestTumorFactory.withBrainLesionStatus(true, null)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestTumorFactory.withBrainLesionStatus(false, null)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestTumorFactory.withBrainLesionStatus(null, true)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestTumorFactory.withBrainLesionStatus(true, true)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestTumorFactory.withBrainLesionStatus(null, false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestTumorFactory.withBrainLesionStatus(true, false)))
    }
}