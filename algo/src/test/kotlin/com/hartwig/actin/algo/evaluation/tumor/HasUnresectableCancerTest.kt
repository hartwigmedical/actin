package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStage
import org.junit.jupiter.api.Test

class HasUnresectableCancerTest {

    val function = HasUnresectableCancer()

    @Test
    fun `Should be undetermined without stage information`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withTumorStage(null)),
            "Undetermined if cancer is unresectable (tumor stage missing)"
        )
    }

    @Test
    fun `Should pass with stage IV`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IV)),
            "Has unresectable cancer (stage IV)"
        )
    }

    @Test
    fun `Should be undetermined with stage III`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IIIA)),
            "Undetermined if cancer is unresectable (stage IIIA)"
        )
    }

    @Test
    fun `Should fail with stage I or II`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IB)),
            "No unresectable cancer (stage IB)"
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withTumorStage(TumorStage.II)),
            "No unresectable cancer (stage II)"
        )
    }
}