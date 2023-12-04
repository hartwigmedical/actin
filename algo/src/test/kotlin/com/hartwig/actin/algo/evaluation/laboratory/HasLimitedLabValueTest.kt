package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.junit.Test

class HasLimitedLabValueTest {
    @Test
    fun canEvaluateStandardCase() {
        val measurement = LabMeasurement.THROMBOCYTES_ABS
        val function = HasLimitedLabValue(1.0, measurement, measurement.defaultUnit())
        val record = TestDataFactory.createMinimalTestPatientRecord()
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(record, measurement, LabTestFactory.forMeasurement(measurement).value(2.0).build())
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                record, measurement,
                LabTestFactory.forMeasurement(measurement).value(0.5).comparator(ValueComparison.LARGER_THAN).build()
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record, measurement, LabTestFactory.forMeasurement(measurement).value(0.5).build())
        )
    }

    @Test
    fun canEvaluateCaseRequiringConversion() {
        val measurement = LabMeasurement.CREATININE
        val function = HasLimitedLabValue(1.0, measurement, LabUnit.MILLIGRAMS_PER_DECILITER)
        val record = TestDataFactory.createMinimalTestPatientRecord()
        val targetUnit = LabTestFactory.forMeasurement(measurement).unit(LabUnit.MILLIGRAMS_PER_DECILITER)
        val offUnit = LabTestFactory.forMeasurement(measurement).unit(LabUnit.MICROMOLES_PER_LITER)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, measurement, targetUnit.value(2.0).build()))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, measurement, targetUnit.value(0.5).build()))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, measurement, offUnit.value(80.0).build()))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, measurement, offUnit.value(120.0).build()))

        // Test that evaluation becomes undetermined if lab evaluation cannot convert.
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(record, measurement, LabTestFactory.forMeasurement(measurement).unit(LabUnit.NONE).value(10.0).build())
        )
    }
}