package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class AdheresToBloodDonationPrescriptionsTest {

    @Test
    fun `Should return not evaluated`() {
        assertEvaluation(
            EvaluationResult.NOT_EVALUATED,
            AdheresToBloodDonationPrescriptions().evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}