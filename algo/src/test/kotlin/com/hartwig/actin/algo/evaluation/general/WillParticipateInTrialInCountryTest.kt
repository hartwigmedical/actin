package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class WillParticipateInTrialInCountryTest {

    @Test
    fun canEvaluate() {
        val netherlands = WillParticipateInTrialInCountry("The Netherlands")
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            netherlands.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
        val germany = WillParticipateInTrialInCountry("Germany")
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, germany.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }
}