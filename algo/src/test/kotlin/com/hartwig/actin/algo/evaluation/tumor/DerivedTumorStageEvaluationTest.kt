package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFactory.pass
import com.hartwig.actin.algo.evaluation.EvaluationFactory.undetermined
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DerivedTumorStageEvaluationTest {

    @Test
    fun `Should use message from worst outcome along with derivation note`() {
        val evaluation = DerivedTumorStageEvaluation.create(
            mapOf(
                TumorStage.I to pass("Pass specific message", "Pass general message"),
                TumorStage.II to undetermined("Undetermined specific message", "Undetermined general message")
            ), EvaluationFactory::undetermined
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedSpecificMessages).containsOnly(
            "Undetermined specific message. Tumor stage has been implied to be I or II"
        )
        assertThat(evaluation.undeterminedGeneralMessages).containsOnly("Undetermined general message")
    }
}