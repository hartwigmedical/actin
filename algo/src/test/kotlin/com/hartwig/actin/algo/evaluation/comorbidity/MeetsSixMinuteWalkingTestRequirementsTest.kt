package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class MeetsSixMinuteWalkingTestRequirementsTest {

    @Test
    fun `Should evaluate to undetermined for minimal patient record`() {
        val function = MeetsSixMinuteWalkingTestRequirements(EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).comorbidity)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}