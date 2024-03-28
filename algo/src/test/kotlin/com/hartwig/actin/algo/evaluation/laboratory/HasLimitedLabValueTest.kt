package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.junit.Test

class HasLimitedLabValueTest {

    @Test
    fun `Should evaluate standard case`() {
        val measurement = LabMeasurement.THROMBOCYTES_ABS
        val function = HasLimitedLabValue(1.0, measurement, measurement.defaultUnit)
        val record = TestPatientFactory.createMinimalTestPatientRecord()
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(record, measurement, LabTestFactory.create(measurement, 2.0))
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                record, measurement,
                LabTestFactory.create(measurement, 0.5).copy(comparator = ValueComparison.LARGER_THAN)
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record, measurement, LabTestFactory.create(measurement, 0.5))
        )
    }

    @Test
    fun `Should evaluate case requiring conversion`() {
        val measurement = LabMeasurement.CREATININE
        val function = HasLimitedLabValue(1.0, measurement, LabUnit.MILLIGRAMS_PER_DECILITER)
        val record = TestPatientFactory.createMinimalTestPatientRecord()
        val targetUnit = LabTestFactory.create(measurement).copy(unit = LabUnit.MILLIGRAMS_PER_DECILITER)
        val offUnit = LabTestFactory.create(measurement).copy(unit = LabUnit.MICROMOLES_PER_LITER)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, measurement, targetUnit.copy(value = 2.0)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, measurement, targetUnit.copy(value = 0.5)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, measurement, offUnit.copy(value = 80.0)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, measurement, offUnit.copy(value = 120.0)))

        // Test that evaluation becomes undetermined if lab evaluation cannot convert.
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(record, measurement, LabTestFactory.create(measurement, 10.0).copy(unit = LabUnit.NONE))
        )
    }
}