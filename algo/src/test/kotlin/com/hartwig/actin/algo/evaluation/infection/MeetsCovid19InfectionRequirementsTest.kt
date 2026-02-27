package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class MeetsCovid19InfectionRequirementsTest {
    @Test
    fun canEvaluate() {
        val function = MeetsCovid19InfectionRequirements()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }
}