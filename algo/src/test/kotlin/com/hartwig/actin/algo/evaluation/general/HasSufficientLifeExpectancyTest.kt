package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import org.junit.Test

class HasSufficientLifeExpectancyTest {

    @Test
    fun canEvaluate() {
        val function = HasSufficientLifeExpectancy()
        EvaluationAssert.assertEvaluation(
            EvaluationResult.NOT_EVALUATED,
            function.evaluate(TestDataFactory.createMinimalTestPatientRecord())
        )
    }
}