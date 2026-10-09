package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HasSufficientLabValueTest {

    private val measurement = LabMeasurement.THROMBOCYTES_ABS
    private val function = HasSufficientLabValue(200.0, measurement, measurement.defaultUnit)
    private val record = TestPatientFactory.createMinimalTestWGSPatientRecord()

    @Test
    fun `Should pass if lab value is above minimal value`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record, measurement, LabTestFactory.create(measurement, 300.0)),
            "Absolute thrombocyte count 300.0 10^9/L exceeds min of 200.0 10^9/L"
        )
    }

    @Test
    fun `Should evaluate to recoverable undetermined if lab value is under minimal value but within 10 percent error margin`() {
        val evaluation = function.evaluate(record, measurement, LabTestFactory.create(measurement, 190.0))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "Absolute thrombocyte count 190.0 10^9/L below min of 200.0 10^9/L but within margin of error"
        )
        assertThat(evaluation.recoverable).isTrue()
    }

    @Test
    fun `Should evaluate to undetermined if comparison can not be determined`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                record, measurement,
                LabTestFactory.create(measurement, 300.0).copy(comparator = ValueComparison.SMALLER_THAN)
            ),
            "Absolute thrombocyte count undetermined"
        )
    }

    @Test
    fun `Should fail if lab value is below minimal value and outside error margin`() {
        val actual = function.evaluate(record, measurement, LabTestFactory.create(measurement, 100.0))
        assertEvaluation(EvaluationResult.FAIL, actual, "Absolute thrombocyte count 100.0 10^9/L below min of 200.0 10^9/L")
        assertThat(actual.recoverable).isTrue()
    }

    @Test
    fun `Should correctly evaluate case requiring conversion`() {
        val measurement = LabMeasurement.HEMOGLOBIN
        val function = HasSufficientLabValue(7.5, measurement, LabUnit.MILLIMOLES_PER_LITER)
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord()
        val targetUnit = LabTestFactory.create(measurement).copy(unit = LabUnit.MILLIMOLES_PER_LITER)
        val offUnit = LabTestFactory.create(measurement).copy(unit = LabUnit.GRAMS_PER_DECILITER)

        // Standard
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record, measurement, targetUnit.copy(value = 8.5)),
            "Hemoglobin 8.5 mmol/L exceeds min of 7.5 mmol/L"
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record, measurement, targetUnit.copy(value = 7.5)),
            "Hemoglobin 7.5 mmol/L exceeds min of 7.5 mmol/L"
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(record, measurement, targetUnit.copy(value = 6.5)),
            "Hemoglobin 6.5 mmol/L below min of 7.5 mmol/L"
        )

        // Different unit
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record, measurement, offUnit.copy(value = 12.2)),
            "Hemoglobin 7.6 mmol/L (converted from: 12.2 g/dL) exceeds min of 7.5 mmol/L"
        )

        val evaluation = function.evaluate(record, measurement, offUnit.copy(value = 8.2))
        assertEvaluation(EvaluationResult.FAIL, evaluation, "Hemoglobin 5.1 mmol/L (converted from: 8.2 g/dL) below min of 7.5 mmol/L")

        // Works with other unit as target unit as well.
        val function2 = HasSufficientLabValue(7.5, measurement, LabUnit.GRAMS_PER_DECILITER)
        assertEvaluation(
            EvaluationResult.PASS,
            function2.evaluate(record, measurement, targetUnit.copy(value = 6.5)),
            "Hemoglobin 10.5 g/dL (converted from: 6.5 mmol/L) exceeds min of 7.5 g/dL"
        )

        // Test that evaluation becomes undetermined if lab evaluation cannot convert.
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(record, measurement, LabTestFactory.create(measurement, 10.0).copy(unit = LabUnit.NONE)),
            "Could not convert value for hemoglobin to mmol/L"
        )
    }
}