package com.hartwig.actin.algo.evaluation.reproduction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class IsBreastfeedingTest {

    private val function = IsBreastfeeding()

    @Test
    fun `Should fail always`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }
}