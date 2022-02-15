package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasLabValueWithinRefTest {

    @Test
    public void canEvaluate() {
        HasLabValueWithinRef function = new HasLabValueWithinRef();
        PatientRecord record = TestDataFactory.createMinimalTestPatientRecord();

        assertEquals(EvaluationResult.UNDETERMINED,
                function.evaluate(record, LabTestFactory.builder().isOutsideRef(null).build()).result());
        assertEquals(EvaluationResult.PASS, function.evaluate(record, LabTestFactory.builder().isOutsideRef(false).build()).result());
        assertEquals(EvaluationResult.FAIL, function.evaluate(record, LabTestFactory.builder().isOutsideRef(true).build()).result());
    }
}