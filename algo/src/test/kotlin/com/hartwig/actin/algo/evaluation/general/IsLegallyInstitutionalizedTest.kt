package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class IsLegallyInstitutionalizedTest {

    @Test
    fun canEvaluate() {
        val function = IsLegallyInstitutionalized()
        EvaluationAssert.assertEvaluation(
            EvaluationResult.NOT_EVALUATED, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}