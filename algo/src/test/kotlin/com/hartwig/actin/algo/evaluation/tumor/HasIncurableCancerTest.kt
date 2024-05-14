package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TumorStage
import org.junit.Test

class HasIncurableCancerTest {
    @Test
    fun canEvaluate() {
        val function = HasIncurableCancer()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestTumorFactory.withTumorStage(null)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestTumorFactory.withTumorStage(TumorStage.IV)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestTumorFactory.withTumorStage(TumorStage.IIIB)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestTumorFactory.withTumorStage(TumorStage.II)))
    }
}