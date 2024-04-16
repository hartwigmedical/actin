package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasLabValueWithinInstitutionalNormalLimitTest {

    @Test
    fun canEvaluate() {
        val function = HasLabValueWithinInstitutionalNormalLimit()
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord()
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 0.0))
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 0.0).copy(isOutsideRef = false))
        )
        val actual = function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 0.0).copy(isOutsideRef = true))
        assertEvaluation(EvaluationResult.FAIL, actual)
        assertThat(actual.recoverable).isTrue
    }
}