package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;

import org.junit.Test;

public class HasLimitedLabValueULNTest {

    @Test
    public void canEvaluate() {
        HasLimitedLabValueULN function = new HasLimitedLabValueULN(1.2);

        PatientRecord record = TestDataFactory.createMinimalTestPatientRecord();
        assertEquals(Evaluation.PASS, function.evaluate(record, LabTestFactory.builder().refLimitUp(75D).value(80D).build()));
        assertEquals(Evaluation.FAIL, function.evaluate(record, LabTestFactory.builder().refLimitUp(75D).value(100D).build()));
    }
}