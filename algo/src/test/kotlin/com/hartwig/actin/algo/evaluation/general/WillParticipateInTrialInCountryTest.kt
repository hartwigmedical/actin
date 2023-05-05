package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import org.junit.Test

class WillParticipateInTrialInCountryTest {
    @Test
    fun canEvaluate() {
        val netherlands = WillParticipateInTrialInCountry("The Netherlands")
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, netherlands.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
        val germany = WillParticipateInTrialInCountry("Germany")
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, germany.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }
}