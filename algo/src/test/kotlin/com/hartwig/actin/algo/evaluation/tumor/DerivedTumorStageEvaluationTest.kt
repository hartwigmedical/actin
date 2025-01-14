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
                TumorStage.I to pass("Pass message"),
                TumorStage.II to undetermined("Undetermined message")
            ), EvaluationFactory::undetermined
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages).containsOnly("Undetermined message")
    }
}