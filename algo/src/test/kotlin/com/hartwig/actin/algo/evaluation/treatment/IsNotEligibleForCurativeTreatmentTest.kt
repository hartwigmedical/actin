package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class IsNotEligibleForCurativeTreatmentTest {

    @Test
    fun shouldAlwaysReturnNotEvaluated() {
        val function = IsNotEligibleForCurativeTreatment()
        assertEvaluation(
            EvaluationResult.NOT_EVALUATED,
            function.evaluate(TestDataFactory.createMinimalTestPatientRecord())
        )
    }
}