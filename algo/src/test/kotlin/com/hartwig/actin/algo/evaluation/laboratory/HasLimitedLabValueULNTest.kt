package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HasLimitedLabValueULNTest {

    private val function = HasLimitedLabValueULN(1.2)
    private val record = TestPatientFactory.createMinimalTestWGSPatientRecord()

    @Test
    fun `Should pass when lab value is under requested fold of ULN`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(measurement = LabMeasurement.CREATININE, value = 110.0, refLimitUp = 100.0)),
            "Creatinine 110.0 µmol/L below max of 1.2*ULN (120.0 µmol/L)"
        )
    }

    @Test
    fun `Should evaluate to recoverable undetermined if lab value is above requested fold of ULN but within margin of error`() {
        val evaluation = function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(measurement = LabMeasurement.CREATININE, value = 125.0, refLimitUp = 100.0))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "Creatinine 125.0 µmol/L exceeds max of 1.2*ULN (120.0 µmol/L)"
        )
        assertThat(evaluation.recoverable).isTrue
    }

    @Test
    fun `Should evaluate to undetermined if comparison to ULN cannot be made due to missing reference limit`() {
        val evaluation = function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 100.0))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation, "Creatinine undetermined")
        assertThat(evaluation.recoverable).isTrue
    }

    @Test
    fun `Should fail if lab value is above requested fold of ULN and outside margin of error`() {
        val actual = function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(measurement = LabMeasurement.CREATININE, value = 135.0, refLimitUp = 100.0))
        assertEvaluation(EvaluationResult.FAIL, actual, "Creatinine 135.0 µmol/L exceeds max of 1.2*ULN (120.0 µmol/L)")
        assertThat(actual.recoverable).isTrue
    }
}