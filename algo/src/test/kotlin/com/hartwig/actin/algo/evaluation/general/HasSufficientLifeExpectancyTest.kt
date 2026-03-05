package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasSufficientLifeExpectancyTest {

    private val function = HasSufficientLifeExpectancy()

    @Test
    fun `Should pass always`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}