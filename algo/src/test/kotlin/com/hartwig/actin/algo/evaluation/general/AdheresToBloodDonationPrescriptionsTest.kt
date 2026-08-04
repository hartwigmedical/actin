package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class AdheresToBloodDonationPrescriptionsTest {

    private val labels = EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).general

    @Test
    fun `Should return pass always`() {
        assertEvaluation(
            EvaluationResult.PASS,
            AdheresToBloodDonationPrescriptions(labels).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}