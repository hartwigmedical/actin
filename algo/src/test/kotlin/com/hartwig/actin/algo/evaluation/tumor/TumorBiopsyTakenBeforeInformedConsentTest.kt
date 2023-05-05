package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class TumorBiopsyTakenBeforeInformedConsentTest {
    @Test
    fun canEvaluate() {
        val function = TumorBiopsyTakenBeforeInformedConsent()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }
}