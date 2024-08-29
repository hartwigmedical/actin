package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasLabValueWithinInstitutionalNormalLimitTest {

    private val function = HasLabValueWithinInstitutionalNormalLimit()
    private val record = TestPatientFactory.createMinimalTestWGSPatientRecord()
    private val labValue = LabTestFactory.create(value = 0.0)

    @Test
    fun `Should evaluate to undetermined if isOutsideRef is null`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(record, LabMeasurement.CREATININE, labValue.copy(isOutsideRef = null))
        )
    }

    @Test
    fun `Should pass if isOutsideRef is false`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record, LabMeasurement.CREATININE, labValue.copy(isOutsideRef = false))
        )
    }

    @Test
    fun `Should fail if isOutsideRef is true`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(record, LabMeasurement.CREATININE, labValue.copy(isOutsideRef = true))
        )
    }
}