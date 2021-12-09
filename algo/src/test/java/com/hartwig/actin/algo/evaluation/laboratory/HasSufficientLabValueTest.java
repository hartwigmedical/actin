package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;

import org.junit.Test;

public class HasSufficientLabValueTest {

    @Test
    public void canEvaluate() {
        HasSufficientLabValue function = new HasSufficientLabValue(200D);

        PatientRecord record = TestDataFactory.createMinimalTestPatientRecord();
        assertEquals(Evaluation.PASS, function.evaluate(record, LabTestFactory.builder().value(300D).build()));
        assertEquals(Evaluation.FAIL, function.evaluate(record, LabTestFactory.builder().value(100D).build()));
    }
}