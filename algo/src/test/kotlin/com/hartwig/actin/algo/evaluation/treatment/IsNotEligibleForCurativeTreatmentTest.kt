package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class IsNotEligibleForCurativeTreatmentTest {

    private val function = IsNotEligibleForCurativeTreatment()

    @Test
    fun `Should pass always`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }
}