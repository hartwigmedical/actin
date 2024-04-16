package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.assertj.core.api.Assertions
import org.junit.Test

class HasSufficientLabValueULNTest {

    private val function = HasSufficientLabValueULN(1.0)
    private val record = TestPatientFactory.createMinimalTestWGSPatientRecord()

    @Test
    fun `Should pass when lab value is above requested fold of ULN`() {
        Assertions.assertThat(
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 220.0, refLimitUp = 200.0)).result
        ).isEqualTo(EvaluationResult.PASS)
    }

    @Test
    fun `Should evaluate to recoverable undetermined if lab value is under requested fold of ULN but within margin of error`() {
        Assertions.assertThat(
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 195.0, refLimitUp = 200.0)).result
        ).isEqualTo(EvaluationResult.UNDETERMINED)

        Assertions.assertThat(
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 195.0, refLimitUp = 200.0)).recoverable
        ).isTrue()
    }

    @Test
    fun `Should evaluate to undetermined if comparison to ULN cannot be made due to missing reference limit`() {
        Assertions.assertThat(
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 80.0)).result
        ).isEqualTo(EvaluationResult.UNDETERMINED)

        Assertions.assertThat(
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 80.0)).recoverable
        ).isTrue()
    }

    @Test
    fun `Should fail if lab value is under requested fold of ULN and outside margin of error`() {
        Assertions.assertThat(
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 175.0, refLimitUp = 200.0)).result
        ).isEqualTo(EvaluationResult.FAIL)

        Assertions.assertThat(
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 175.0, refLimitUp = 200.0)).recoverable
        ).isTrue()

    }

}