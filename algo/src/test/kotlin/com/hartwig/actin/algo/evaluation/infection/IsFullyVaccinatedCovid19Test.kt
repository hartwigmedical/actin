package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class IsFullyVaccinatedCovid19Test {
    @Test
    fun canEvaluate() {
        val function = IsFullyVaccinatedCovid19()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }
}