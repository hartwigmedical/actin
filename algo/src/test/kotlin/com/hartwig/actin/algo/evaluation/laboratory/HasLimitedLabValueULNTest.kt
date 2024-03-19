package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasLimitedLabValueULNTest {

    @Test
    fun canEvaluate() {
        val function = HasLimitedLabValueULN(1.2)
        val record = TestPatientFactory.createMinimalTestPatientRecord()
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 80.0, refLimitUp = 75.0))
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 80.0))
        )
        val actual = function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 100.0, refLimitUp = 75.0))
        assertEvaluation(EvaluationResult.FAIL, actual)
        assertThat(actual.recoverable).isTrue
    }
}