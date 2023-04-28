package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.junit.Test

class HasSufficientLabValueTest {
    @Test
    fun canEvaluate() {
        val measurement = LabMeasurement.THROMBOCYTES_ABS
        val function = HasSufficientLabValue(200.0, measurement, measurement.defaultUnit())
        val record = TestDataFactory.createMinimalTestPatientRecord()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, LabTestFactory.forMeasurement(measurement).value(300.0).build()))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                record,
                LabTestFactory.forMeasurement(measurement).value(300.0).comparator(ValueComparison.SMALLER_THAN).build()
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, LabTestFactory.forMeasurement(measurement).value(100.0).build()))
    }

    @Test
    fun canEvaluateCaseRequiringConversion() {
        val measurement = LabMeasurement.HEMOGLOBIN
        val function = HasSufficientLabValue(7.5, measurement, LabUnit.MILLIMOLES_PER_LITER)
        val record = TestDataFactory.createMinimalTestPatientRecord()
        val targetUnit = LabTestFactory.forMeasurement(measurement).unit(LabUnit.MILLIMOLES_PER_LITER)
        val offUnit = LabTestFactory.forMeasurement(measurement).unit(LabUnit.GRAMS_PER_DECILITER)

        // Standard
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, targetUnit.value(8.5).build()))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, targetUnit.value(7.5).build()))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, targetUnit.value(6.5).build()))

        // Different unit
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, offUnit.value(12.2).build()))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, offUnit.value(8.2).build()))

        // Works with other unit as target unit as well.
        val function2 = HasSufficientLabValue(7.5, measurement, LabUnit.GRAMS_PER_DECILITER)
        assertEvaluation(EvaluationResult.PASS, function2.evaluate(record, targetUnit.value(6.5).build()))

        // Test that evaluation becomes undetermined if lab evaluation cannot convert.
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(record, LabTestFactory.forMeasurement(measurement).unit(LabUnit.NONE).value(10.0).build())
        )
    }
}