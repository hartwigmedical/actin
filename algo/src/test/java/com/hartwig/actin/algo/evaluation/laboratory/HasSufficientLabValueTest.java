package com.hartwig.actin.algo.evaluation.laboratory;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.LabUnit;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class HasSufficientLabValueTest {

    @Test
    public void canEvaluate() {
        LabMeasurement measurement = LabMeasurement.THROMBOCYTES_ABS;
        HasSufficientLabValue function = new HasSufficientLabValue(200D, measurement, measurement.defaultUnit());

        PatientRecord record = TestDataFactory.createMinimalTestPatientRecord();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, LabTestFactory.forMeasurement(measurement).value(300D).build()));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, LabTestFactory.forMeasurement(measurement).value(100D).build()));
    }

    @Test
    public void canEvaluateCaseRequiringConversion() {
        LabMeasurement measurement = LabMeasurement.HEMOGLOBIN;
        HasSufficientLabValue function = new HasSufficientLabValue(7.5, measurement, LabUnit.MILLIMOLES_PER_LITER);
        PatientRecord record = TestDataFactory.createMinimalTestPatientRecord();

        ImmutableLabValue.Builder targetUnit = LabTestFactory.forMeasurement(measurement).unit(LabUnit.MILLIMOLES_PER_LITER);
        ImmutableLabValue.Builder offUnit = LabTestFactory.forMeasurement(measurement).unit(LabUnit.GRAMS_PER_DECILITER);

        // Standard
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, targetUnit.value(8.5).build()));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, targetUnit.value(7.5).build()));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, targetUnit.value(6.5).build()));

        // Different unit
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, offUnit.value(12.2).build()));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, offUnit.value(8.2).build()));

        // Works with other unit as target unit as well.
        HasSufficientLabValue function2 = new HasSufficientLabValue(7.5, measurement, LabUnit.GRAMS_PER_DECILITER);
        assertEvaluation(EvaluationResult.PASS, function2.evaluate(record, targetUnit.value(6.5).build()));

        // Test that evaluation becomes undetermined if lab evaluation cannot convert.
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(record, LabTestFactory.forMeasurement(measurement).unit(LabUnit.NONE).value(10D).build()));
    }
}