package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class IsNotParticipatingInAnotherTrialTest {

    @Test
    fun shouldAlwaysReturnNotEvaluated() {
        val function = IsNotParticipatingInAnotherTrial()
        assertEvaluation(
            EvaluationResult.NOT_EVALUATED,
            function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}