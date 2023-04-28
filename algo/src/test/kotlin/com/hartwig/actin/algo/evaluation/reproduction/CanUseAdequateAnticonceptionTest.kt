package com.hartwig.actin.algo.evaluation.reproduction

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class CanUseAdequateAnticonceptionTest {
    @Test
    fun canEvaluate() {
        val function = CanUseAdequateAnticonception()
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }
}