package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class IsEligibleForIntensiveTreatmentTest {

    @Test
    fun `Should evaluate to undetermined`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            IsEligibleForIntensiveTreatment().evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}