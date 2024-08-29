package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class IsEligibleForPalliativeRadiotherapyTest {
    @Test
    fun shouldEvaluateToUndetermined() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            IsEligibleForPalliativeRadiotherapy().evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}