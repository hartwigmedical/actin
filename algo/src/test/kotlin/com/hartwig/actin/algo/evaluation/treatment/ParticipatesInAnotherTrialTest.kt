package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class ParticipatesInAnotherTrialTest {
    @Test
    fun canEvaluate() {
        val function = ParticipatesInAnotherTrial()
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }
}