package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Assert.assertTrue
import org.junit.Test

class HasLabValueOutsideRefLimitUpTest {
    @Test
    fun canEvaluate() {
        val function = HasLabValueOutsideRefLimitUp()
        val record = TestDataFactory.createMinimalTestPatientRecord()
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(record, LabTestFactory.builder().value(5.0).refLimitUp(null).build())
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, LabTestFactory.builder().value(5.0).refLimitUp(3.0).build()))
        val actual = function.evaluate(record, LabTestFactory.builder().value(5.0).refLimitUp(7.0).build())
        assertEvaluation(EvaluationResult.FAIL, actual)
        assertTrue(actual.recoverable())
    }
}