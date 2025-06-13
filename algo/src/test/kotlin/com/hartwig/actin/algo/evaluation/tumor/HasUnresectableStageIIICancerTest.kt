package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStage
import org.junit.Test

class HasUnresectableStageIIICancerTest {

    val function = HasUnresectableStageIIICancer()

    @Test
    fun `Should be undetermined without stage information`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withTumorStage(null)))
    }

    @Test
    fun `Should be undetermined with stage III`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IIIC)))
    }

    @Test
    fun `Should fail with stage I, II or IV`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.I)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IIA)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IVB)))
    }
}