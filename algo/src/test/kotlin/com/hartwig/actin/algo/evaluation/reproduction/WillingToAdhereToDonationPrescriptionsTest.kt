package com.hartwig.actin.algo.evaluation.reproduction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class WillingToAdhereToDonationPrescriptionsTest {
    @Test
    fun canEvaluate() {
        val function = WillingToAdhereToDonationPrescriptions()
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }
}