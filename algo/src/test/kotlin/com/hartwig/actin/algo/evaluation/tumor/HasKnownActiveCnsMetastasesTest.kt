package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasKnownActiveCnsMetastasesTest {
    @Test
    fun canEvaluate() {
        val function = HasKnownActiveCnsMetastases()
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TestTumorFactory.withActiveBrainAndCnsLesionStatus(null, null, null, null))
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TestTumorFactory.withActiveBrainAndCnsLesionStatus(true, null, true, null))
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TestTumorFactory.withActiveBrainAndCnsLesionStatus(false, null, false, null))
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TestTumorFactory.withActiveBrainAndCnsLesionStatus(true, null, true, false))
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TestTumorFactory.withActiveBrainAndCnsLesionStatus(true, false, true, null))
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TestTumorFactory.withActiveBrainAndCnsLesionStatus(true, false, true, false))
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TestTumorFactory.withActiveBrainAndCnsLesionStatus(true, null, true, true))
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TestTumorFactory.withActiveBrainAndCnsLesionStatus(true, true, true, null))
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TestTumorFactory.withActiveBrainAndCnsLesionStatus(true, false, true, true))
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TestTumorFactory.withActiveBrainAndCnsLesionStatus(true, true, true, false))
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TestTumorFactory.withActiveBrainAndCnsLesionStatus(true, true, true, true))
        )
    }
}