package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class HasSufficientHemoglobinTest {

    @Test
    public void canEvaluate() {
        HasSufficientHemoglobin function = new HasSufficientHemoglobin(7.5, LabUnit.MMOL_PER_L);
        PatientRecord record = TestDataFactory.createMinimalTestPatientRecord();

        ImmutableLabValue.Builder hemoglobin = LabTestFactory.forMeasurement(LabMeasurement.HEMOGLOBIN);

        // Standard
        assertEquals(EvaluationResult.PASS,
                function.evaluate(record, hemoglobin.unit(LabUnit.MMOL_PER_L.display()).value(8.5).build()).result());
        assertEquals(EvaluationResult.PASS,
                function.evaluate(record, hemoglobin.unit(LabUnit.MMOL_PER_L.display()).value(7.5).build()).result());
        assertEquals(EvaluationResult.FAIL,
                function.evaluate(record, hemoglobin.unit(LabUnit.MMOL_PER_L.display()).value(6.5).build()).result());

        // Different unit
        assertEquals(EvaluationResult.PASS,
                function.evaluate(record, hemoglobin.unit(LabUnit.G_PER_DL.display()).value(12.2).build()).result());
        assertEquals(EvaluationResult.FAIL,
                function.evaluate(record, hemoglobin.unit(LabUnit.G_PER_DL.display()).value(8.2).build()).result());

        // No recognized unit
        assertEquals(EvaluationResult.UNDETERMINED, function.evaluate(record, hemoglobin.unit("not a unit").value(4.2).build()).result());

        // Works with other unit as target unit as well.
        HasSufficientHemoglobin function2 = new HasSufficientHemoglobin(7.5, LabUnit.G_PER_DL);
        assertEquals(EvaluationResult.PASS,
                function2.evaluate(record, hemoglobin.unit(LabUnit.MMOL_PER_L.display()).value(6.5).build()).result());
    }
}