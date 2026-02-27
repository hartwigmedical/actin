package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class IsEligibleForLocoRegionalTherapyTest {

    @Test
    fun shouldEvaluateToUndetermined() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            IsEligibleForLocoRegionalTherapy().evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}