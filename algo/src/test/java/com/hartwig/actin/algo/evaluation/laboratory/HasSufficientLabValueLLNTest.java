package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasSufficientLabValueLLNTest {

    @Test
    public void canEvaluate() {
        HasSufficientLabValueLLN function = new HasSufficientLabValueLLN(2);
        PatientRecord record = TestDataFactory.createMinimalTestPatientRecord();

        assertEquals(EvaluationResult.PASS, function.evaluate(record, LabTestFactory.builder().value(80D).refLimitLow(35D).build()));
        assertEquals(EvaluationResult.FAIL, function.evaluate(record, LabTestFactory.builder().value(100D).refLimitLow(75D).build()));
    }
}