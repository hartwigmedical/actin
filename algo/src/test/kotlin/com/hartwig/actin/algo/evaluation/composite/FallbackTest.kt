package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import org.junit.Test

class FallbackTest {
    @Test
    fun canEvaluate() {
        val pass = Fallback(CompositeTestFactory.create(EvaluationResult.PASS, 1), CompositeTestFactory.create(EvaluationResult.FAIL, 2))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, pass.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
        val fallback =
            Fallback(CompositeTestFactory.create(EvaluationResult.UNDETERMINED, 1), CompositeTestFactory.create(EvaluationResult.FAIL, 2))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, fallback.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }
}