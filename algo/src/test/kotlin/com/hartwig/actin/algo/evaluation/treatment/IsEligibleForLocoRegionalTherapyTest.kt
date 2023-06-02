package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class IsEligibleForLocoRegionalTherapyTest {

    @Test
    fun shouldEvaluateToUndetermined() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            IsEligibleForLocoRegionalTherapy().evaluate(TestDataFactory.createMinimalTestPatientRecord())
        )
    }
}