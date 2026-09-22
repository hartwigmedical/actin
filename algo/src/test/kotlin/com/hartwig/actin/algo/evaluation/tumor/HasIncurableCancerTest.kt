package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStage
import org.junit.jupiter.api.Test

class HasIncurableCancerTest {

    val function = HasIncurableCancer()

    @Test
    fun `Should be undetermined without stage information`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withTumorStage(null)),
            "Incurable cancer undetermined (tumor stage missing)"
        )
    }

    @Test
    fun `Should pass with stage IV`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IV)),
            "Stage IV cancer is considered incurable"
        )
    }

    @Test
    fun `Should be undetermined with stage III`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IIIA)),
            "Undetermined if stage IIIA is considered incurable"
        )
    }

    @Test
    fun `Should fail with stage I or II`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IB)),
            "Stage IB cancer is not considered incurable"
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withTumorStage(TumorStage.II)),
            "Stage II cancer is not considered incurable"
        )
    }
}