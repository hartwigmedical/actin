package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import org.junit.Test

class CanGiveAdequateInformedConsentTest {
    @Test
    fun canEvaluate() {
        val function = CanGiveAdequateInformedConsent()
        EvaluationAssert.assertEvaluation(
            EvaluationResult.NOT_EVALUATED,
            function.evaluate(TestPatientFactory.createMinimalTestPatientRecord())
        )
    }
}