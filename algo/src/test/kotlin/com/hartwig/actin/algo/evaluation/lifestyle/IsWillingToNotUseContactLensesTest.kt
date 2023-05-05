package com.hartwig.actin.algo.evaluation.lifestyle

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class IsWillingToNotUseContactLensesTest {
    @Test
    fun canEvaluate() {
        val function = IsWillingToNotUseContactLenses()
        assertEvaluation(EvaluationResult.WARN, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }
}