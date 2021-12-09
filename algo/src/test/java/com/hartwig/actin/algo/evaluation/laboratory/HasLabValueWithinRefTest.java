package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;

import org.junit.Test;

public class HasLabValueWithinRefTest {

    @Test
    public void canEvaluate() {
        HasLabValueWithinRef function = new HasLabValueWithinRef();
        PatientRecord record = TestDataFactory.createMinimalTestPatientRecord();

        assertEquals(Evaluation.UNDETERMINED, function.evaluate(record, LabTestFactory.builder().isOutsideRef(null).build()));
        assertEquals(Evaluation.PASS, function.evaluate(record, LabTestFactory.builder().isOutsideRef(false).build()));
        assertEquals(Evaluation.FAIL, function.evaluate(record, LabTestFactory.builder().isOutsideRef(true).build()));
    }
}