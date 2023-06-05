package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import org.junit.Test

class IsEligibleForPalliativeRadiotherapyTest {
    @Test
    fun shouldEvaluateToUndetermined() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            IsEligibleForPalliativeRadiotherapy().evaluate(TestDataFactory.createMinimalTestPatientRecord())
        )
    }
}