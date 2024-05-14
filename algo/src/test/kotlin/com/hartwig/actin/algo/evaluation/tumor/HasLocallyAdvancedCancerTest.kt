package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TumorStage
import org.junit.Test

class HasLocallyAdvancedCancerTest {
    @Test
    fun canEvaluate() {
        val function = HasLocallyAdvancedCancer()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestTumorFactory.withTumorStage(null)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestTumorFactory.withTumorStage(TumorStage.IIIB)))
        assertEvaluation(EvaluationResult.WARN, function.evaluate(TestTumorFactory.withTumorStage(TumorStage.IIB)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestTumorFactory.withTumorStage(TumorStage.IV)))
    }
}