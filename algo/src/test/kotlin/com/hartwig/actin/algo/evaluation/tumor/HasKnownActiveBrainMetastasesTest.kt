package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasKnownActiveBrainMetastasesTest {
    @Test
    fun canEvaluate() {
        val function = HasKnownActiveBrainMetastases()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withBrainLesionStatus(null, null)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withBrainLesionStatus(true, null)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBrainLesionStatus(false, null)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBrainLesionStatus(null, true)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withBrainLesionStatus(true, true)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBrainLesionStatus(null, false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withBrainLesionStatus(true, false)))
    }
}