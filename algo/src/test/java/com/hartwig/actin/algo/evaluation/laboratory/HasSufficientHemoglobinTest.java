package com.hartwig.actin.algo.evaluation.laboratory;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.LabUnit;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class HasSufficientHemoglobinTest {

    @Test
    public void canEvaluate() {
        HasSufficientHemoglobin function = new HasSufficientHemoglobin(7.5, LabUnit.MILLIMOLES_PER_LITER);
        PatientRecord record = TestDataFactory.createMinimalTestPatientRecord();

        ImmutableLabValue.Builder hemoglobin = LabTestFactory.forMeasurement(LabMeasurement.HEMOGLOBIN);

        // Standard
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(record, hemoglobin.unit(LabUnit.MILLIMOLES_PER_LITER).value(8.5).build()));
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(record, hemoglobin.unit(LabUnit.MILLIMOLES_PER_LITER).value(7.5).build()));
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(record, hemoglobin.unit(LabUnit.MILLIMOLES_PER_LITER).value(6.5).build()));

        // Different unit
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(record, hemoglobin.unit(LabUnit.GRAMS_PER_DECILITER).value(12.2).build()));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, hemoglobin.unit(LabUnit.GRAMS_PER_DECILITER).value(8.2).build()));

        // Works with other unit as target unit as well.
        HasSufficientHemoglobin function2 = new HasSufficientHemoglobin(7.5, LabUnit.GRAMS_PER_DECILITER);
        assertEvaluation(EvaluationResult.PASS,
                function2.evaluate(record, hemoglobin.unit(LabUnit.MILLIMOLES_PER_LITER).value(6.5).build()));
    }
}