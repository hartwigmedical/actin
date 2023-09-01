package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Assert.assertTrue
import org.junit.Test

class HasLabValueWithinInstitutionalNormalLimitTest {
    @Test
    fun canEvaluate() {
        val function = HasLabValueWithinInstitutionalNormalLimit()
        val record = TestDataFactory.createMinimalTestPatientRecord()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record, LabTestFactory.builder().isOutsideRef(null).build()))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, LabTestFactory.builder().isOutsideRef(false).build()))
        val actual = function.evaluate(record, LabTestFactory.builder().isOutsideRef(true).build())
        assertEvaluation(EvaluationResult.FAIL, actual)
        assertTrue(actual.recoverable())
    }
}