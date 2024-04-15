package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import org.junit.Test

class IsInvolvedInStudyProceduresTest {

    @Test
    fun canEvaluate() {
        val function = IsInvolvedInStudyProcedures()
        EvaluationAssert.assertEvaluation(
            EvaluationResult.NOT_EVALUATED, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}