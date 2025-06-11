package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStage
import org.junit.Test

class HasLocallyAdvancedCancerTest {

    val function = HasLocallyAdvancedCancer()

    @Test
    fun `Should be undetermined without stage information`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withTumorStage(null)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withTumorStageAndDerivedStages(null, null)))
    }

    @Test
    fun `Should pass with stage III`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IIIB)))
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(TumorStage.IIIB)))
        )
    }

    @Test
    fun `Should be undetermined with stage II`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IIB)))
    }

    @Test
    fun `Should fail with stage I or IV`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.I)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IV)))
    }
}