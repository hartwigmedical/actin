package com.hartwig.actin.algo.evaluation.laboratory;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class HasSufficientLymphocytesTest {

    @Test
    public void canEvaluate() {
        HasSufficientLymphocytes function = new HasSufficientLymphocytes(1.5, LabUnit.BILLION_PER_LITER);
        PatientRecord record = TestDataFactory.createMinimalTestPatientRecord();

        ImmutableLabValue.Builder lymphocytes = LabTestFactory.forMeasurement(LabMeasurement.LYMPHOCYTES_ABS_EDA);

        // Standard
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(record, lymphocytes.unit(LabUnit.BILLION_PER_LITER.display()).value(2.5).build()));
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(record, lymphocytes.unit(LabUnit.BILLION_PER_LITER.display()).value(1.5).build()));
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(record, lymphocytes.unit(LabUnit.BILLION_PER_LITER.display()).value(0.5).build()));

        // Different unit
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(record, lymphocytes.unit(LabUnit.CELLS_PER_MICROLITER.display()).value(2000).build()));
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(record, lymphocytes.unit(LabUnit.CELLS_PER_MICROLITER.display()).value(1000).build()));

        // No recognized unit
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record, lymphocytes.unit("not a unit").value(4.2).build()));

        // Works with other unit as target unit as well.
        HasSufficientLymphocytes function2 = new HasSufficientLymphocytes(1500, LabUnit.CELLS_PER_MICROLITER);
        assertEvaluation(EvaluationResult.PASS,
                function2.evaluate(record, lymphocytes.unit(LabUnit.BILLION_PER_LITER.display()).value(3.0).build()));
    }
}