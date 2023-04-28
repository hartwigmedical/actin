package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasAdequateVeinAccessTest {
    @Test
    fun canEvaluate() {
        val function = HasAdequateVeinAccess()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }
}