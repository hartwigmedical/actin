package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class WillRequireAnticancerTherapyTest {
    @Test
    fun canEvaluate() {
        val function = WillRequireAnticancerTherapy()
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }
}