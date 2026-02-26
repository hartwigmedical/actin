package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class MeetsSixMinuteWalkingTestRequirementsTest {

    @Test
    fun `Should evaluate to undetermined for minimal patient record`() {
        val function = MeetsSixMinuteWalkingTestRequirements()
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}