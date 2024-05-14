package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TumorStage
import org.junit.Test

class HasUnresectableStageIIICancerTest {
    @Test
    fun canEvaluate() {
        val function = HasUnresectableStageIIICancer()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestTumorFactory.withTumorStage(null)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestTumorFactory.withTumorStage(TumorStage.IIIB)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestTumorFactory.withTumorStage(TumorStage.II)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestTumorFactory.withTumorStage(TumorStage.IV)))
    }
}