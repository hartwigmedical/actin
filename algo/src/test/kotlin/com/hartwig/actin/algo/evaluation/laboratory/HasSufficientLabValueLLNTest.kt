package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.assertj.core.api.Assertions
import org.junit.Test

class HasSufficientLabValueLLNTest {

    private val function = HasSufficientLabValueLLN(2.0)
    private val record = TestPatientFactory.createMinimalTestPatientRecord()
    @Test
    fun `Should pass when lab value is above requested fold of LLN`() {
        Assertions.assertThat(
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 80.0, refLimitLow = 35.0)).result
        ).isEqualTo(EvaluationResult.PASS)
    }

    @Test
    fun `Should evaluate to recoverable undetermined if lab value is under requested fold of LLN but within margin of error`() {
        Assertions.assertThat(
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 65.0, refLimitLow = 35.0)).result
        ).isEqualTo(EvaluationResult.UNDETERMINED)

        Assertions.assertThat(
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 65.0, refLimitLow = 35.0)).recoverable
        ).isTrue()
    }

    @Test
    fun `Should evaluate to undetermined if comparison to LLN cannot be made due to missing reference limit`() {
        Assertions.assertThat(
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 80.0)).result
        ).isEqualTo(EvaluationResult.UNDETERMINED)

        Assertions.assertThat(
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 80.0)).recoverable
        ).isTrue()
    }

    @Test
    fun `Should fail if lab value is under requested fold of LLN and outside margin of error`() {
        Assertions.assertThat(
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 50.0, refLimitLow = 35.0)).result
        ).isEqualTo(EvaluationResult.FAIL)

        Assertions.assertThat(
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 50.0, refLimitLow = 35.0)).recoverable
        ).isTrue()

    }
}