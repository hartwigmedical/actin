package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class CanAdhereToAttenuatedVaccineUseTest {
    @Test
    fun canEvaluate() {
        val function = CanAdhereToAttenuatedVaccineUse()
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }
}