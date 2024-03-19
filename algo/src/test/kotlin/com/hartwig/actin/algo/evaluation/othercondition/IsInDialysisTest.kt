package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class IsInDialysisTest {

    @Test
    fun canEvaluate() {
        val function = IsInDialysis()
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TestPatientFactory.createMinimalTestPatientRecord()))
    }
}