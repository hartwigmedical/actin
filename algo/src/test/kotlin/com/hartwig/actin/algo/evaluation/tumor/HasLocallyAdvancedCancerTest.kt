package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStage
import org.junit.Test

class HasLocallyAdvancedCancerTest {
    @Test
    fun canEvaluate() {
        val function = HasLocallyAdvancedCancer()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withTumorStage(null)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IIIB)))
        assertEvaluation(EvaluationResult.WARN, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IIB)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IV)))
    }
}