package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class IsEligibleForLocoRegionalTherapyTest {

    @Test
    fun shouldEvaluateToUndetermined() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            IsEligibleForLocoRegionalTherapy(EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).treatment)
                .evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}