package com.hartwig.actin.algo.evaluation.laboratory;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.LabUnit;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class HasLimitedLabValueTest {

    @Test
    public void canEvaluateStandardCase() {
        LabMeasurement measurement = LabMeasurement.THROMBOCYTES_ABS;
        HasLimitedLabValue function = new HasLimitedLabValue(1, measurement, measurement.defaultUnit());

        PatientRecord record = TestDataFactory.createMinimalTestPatientRecord();

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, LabTestFactory.forMeasurement(measurement).value(2).build()));
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(record,
                        LabTestFactory.forMeasurement(measurement).value(0.5).comparator(LabEvaluation.LARGER_THAN).build()));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, LabTestFactory.forMeasurement(measurement).value(0.5).build()));
    }

    @Test
    public void canEvaluateCaseRequiringConversion() {
        LabMeasurement measurement = LabMeasurement.CREATININE;
        HasLimitedLabValue function = new HasLimitedLabValue(1, measurement, LabUnit.MILLIGRAMS_PER_DECILITER);
        PatientRecord record = TestDataFactory.createMinimalTestPatientRecord();

        ImmutableLabValue.Builder targetUnit = LabTestFactory.forMeasurement(measurement).unit(LabUnit.MILLIGRAMS_PER_DECILITER);
        ImmutableLabValue.Builder offUnit = LabTestFactory.forMeasurement(measurement).unit(LabUnit.MICROMOLES_PER_LITER);

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, targetUnit.value(2).build()));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, targetUnit.value(0.5).build()));

        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, offUnit.value(80).build()));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, offUnit.value(120).build()));

        // Test that evaluation becomes undetermined if lab evaluation cannot convert.
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(record, LabTestFactory.forMeasurement(measurement).unit(LabUnit.NONE).value(10D).build()));
    }
}