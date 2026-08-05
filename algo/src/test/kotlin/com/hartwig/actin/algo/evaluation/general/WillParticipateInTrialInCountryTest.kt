package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class WillParticipateInTrialInCountryTest {

    private val labels = EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).general

    @Test
    fun canEvaluate() {
        val netherlands = WillParticipateInTrialInCountry("The Netherlands", labels)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            netherlands.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
        val germany = WillParticipateInTrialInCountry("Germany", labels)
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, germany.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }
}