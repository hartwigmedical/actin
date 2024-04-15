package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.assertj.core.api.Assertions
import org.junit.Assert.assertTrue
import org.junit.Test

class HasSufficientLabValueTest {

    private val measurement = LabMeasurement.THROMBOCYTES_ABS
    private val function = HasSufficientLabValue(200.0, measurement, measurement.defaultUnit)
    private val record = TestPatientFactory.createMinimalTestWGSPatientRecord()

    @Test
    fun `Should pass if lab value is above minimal value`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record, measurement, LabTestFactory.create(measurement, 300.0))
        )
    }

    @Test
    fun `Should evaluate to recoverable undetermined if lab value is under minimal value but within 10 percent error margin`() {
        val evaluation = function.evaluate(record, measurement, LabTestFactory.create(measurement, 190.0))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        Assertions.assertThat(evaluation.recoverable).isTrue()
    }

    @Test
    fun `Should evaluate to undetermined if comparison can not be determined`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                record, measurement,
                LabTestFactory.create(measurement, 300.0).copy(comparator = ValueComparison.SMALLER_THAN)
            )
        )
    }

    @Test
    fun `Should fail if lab value is below minimal value and outside error margin`() {
        val actual = function.evaluate(record, measurement, LabTestFactory.create(measurement, 100.0))
        assertEvaluation(EvaluationResult.FAIL, actual)
        assertTrue(actual.recoverable)
    }

    @Test
    fun `Should correctly evaluate case requiring conversion`() {
        val measurement = LabMeasurement.HEMOGLOBIN
        val function = HasSufficientLabValue(7.5, measurement, LabUnit.MILLIMOLES_PER_LITER)
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord()
        val targetUnit = LabTestFactory.create(measurement).copy(unit = LabUnit.MILLIMOLES_PER_LITER)
        val offUnit = LabTestFactory.create(measurement).copy(unit = LabUnit.GRAMS_PER_DECILITER)

        // Standard
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, measurement, targetUnit.copy(value = 8.5)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, measurement, targetUnit.copy(value = 7.5)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, measurement, targetUnit.copy(value = 6.5)))

        // Different unit
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, measurement, offUnit.copy(value = 12.2)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, measurement, offUnit.copy(value = 8.2)))

        // Works with other unit as target unit as well.
        val function2 = HasSufficientLabValue(7.5, measurement, LabUnit.GRAMS_PER_DECILITER)
        assertEvaluation(EvaluationResult.PASS, function2.evaluate(record, measurement, targetUnit.copy(value = 6.5)))

        // Test that evaluation becomes undetermined if lab evaluation cannot convert.
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(record, measurement, LabTestFactory.create(measurement, 10.0).copy(unit = LabUnit.NONE))
        )
    }
}