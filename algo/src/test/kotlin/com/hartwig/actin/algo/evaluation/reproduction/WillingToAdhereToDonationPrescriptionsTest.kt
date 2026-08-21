package com.hartwig.actin.algo.evaluation.reproduction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class WillingToAdhereToDonationPrescriptionsTest {

    private val function = WillingToAdhereToDonationPrescriptions()

    @Test
    fun `Should pass always`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()),
            "Assumed that patient is willing to adhere to sperm/egg donation prescriptions"
        )
    }
}