package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStage
import org.junit.Test

class HasIncurableCancerTest {

    val function = HasIncurableCancer()

    @Test
    fun `Should be undetermined without stage information`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withTumorStage(null)))
    }

    @Test
    fun `Should pass with stage IV`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IV)))
    }

    @Test
    fun `Should be undetermined with stage III`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IIIA)))
    }

    @Test
    fun `Should fail with stage I or II`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IB)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.II)))
    }
}