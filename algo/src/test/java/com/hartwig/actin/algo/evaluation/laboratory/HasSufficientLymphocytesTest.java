package com.hartwig.actin.algo.evaluation.laboratory;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.LabUnit;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class HasSufficientLymphocytesTest {

    @Test
    public void canEvaluate() {
        LabMeasurement measurement = LabMeasurement.LYMPHOCYTES_ABS_EDA;
        HasSufficientLymphocytes function = new HasSufficientLymphocytes(1.5, measurement.defaultUnit());
        PatientRecord record = TestDataFactory.createMinimalTestPatientRecord();

        ImmutableLabValue.Builder lymphocytes = LabTestFactory.forMeasurement(measurement);

        // Standard
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, lymphocytes.value(2.5).build()));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, lymphocytes.value(1.5).build()));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, lymphocytes.value(0.5).build()));

        // Different unit
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(record, lymphocytes.unit(LabUnit.CELLS_PER_CUBIC_MILLIMETER).value(2000).build()));
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(record, lymphocytes.unit(LabUnit.CELLS_PER_CUBIC_MILLIMETER).value(1000).build()));

        // Works with other unit as target unit as well.
        HasSufficientLymphocytes function2 = new HasSufficientLymphocytes(1500, LabUnit.CELLS_PER_CUBIC_MILLIMETER);
        assertEvaluation(EvaluationResult.PASS,
                function2.evaluate(record, lymphocytes.unit(LabUnit.BILLIONS_PER_LITER).value(3.0).build()));
    }
}