package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class AdheresToBloodDonationPrescriptionsTest {

    @Test
    fun `Should return pass always`() {
        assertEvaluation(
            EvaluationResult.PASS,
            AdheresToBloodDonationPrescriptions().evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}