package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class CanAdhereToAttenuatedVaccineUseTest {

    @Test
    fun canEvaluate() {
        val function = CanAdhereToAttenuatedVaccineUse()
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }
}