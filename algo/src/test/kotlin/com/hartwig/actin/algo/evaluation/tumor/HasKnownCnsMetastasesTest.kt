package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasKnownCnsMetastasesTest {
    @Test
    fun canEvaluate() {
        val function = HasKnownCnsMetastases()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestTumorFactory.withBrainAndCnsLesions(null, null)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestTumorFactory.withBrainAndCnsLesions(null, false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestTumorFactory.withBrainAndCnsLesions(false, null)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestTumorFactory.withBrainAndCnsLesions(false, false)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestTumorFactory.withBrainAndCnsLesions(null, true)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestTumorFactory.withBrainAndCnsLesions(true, null)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestTumorFactory.withBrainAndCnsLesions(false, true)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestTumorFactory.withBrainAndCnsLesions(true, false)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestTumorFactory.withBrainAndCnsLesions(true, true)))
    }
}