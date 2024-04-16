package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class MeetsCovid19InfectionRequirementsTest {
    @Test
    fun canEvaluate() {
        val function = MeetsCovid19InfectionRequirements()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }
}