package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.junit.Assert.assertTrue
import org.junit.Test

class HasLimitedLabValueULNTest {
    @Test
    fun canEvaluate() {
        val function = HasLimitedLabValueULN(1.2)
        val record = TestDataFactory.createMinimalTestPatientRecord()
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.builder().refLimitUp(75.0).value(80.0).build())
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.builder().value(80.0).build())
        )
        val actual = function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.builder().refLimitUp(75.0).value(100.0).build())
        assertEvaluation(EvaluationResult.FAIL, actual)
        assertTrue(actual.recoverable())
    }
}