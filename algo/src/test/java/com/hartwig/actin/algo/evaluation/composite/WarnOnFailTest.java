package com.hartwig.actin.algo.evaluation.composite;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.TestEvaluationFunctionFactory;

import org.junit.Test;

public class WarnOnFailTest {

    @Test
    public void canWarnOnFail() {
        PatientRecord patient = TestDataFactory.createTestPatientRecord();

        assertEquals(Evaluation.PASS, new WarnOnFail(TestEvaluationFunctionFactory.pass()).evaluate(patient));
        assertEquals(Evaluation.PASS_BUT_WARN, new WarnOnFail(TestEvaluationFunctionFactory.passButWarn()).evaluate(patient));
        assertEquals(Evaluation.PASS_BUT_WARN, new WarnOnFail(TestEvaluationFunctionFactory.fail()).evaluate(patient));
        assertEquals(Evaluation.UNDETERMINED, new WarnOnFail(TestEvaluationFunctionFactory.undetermined()).evaluate(patient));
        assertEquals(Evaluation.NOT_IMPLEMENTED, new WarnOnFail(TestEvaluationFunctionFactory.notImplemented()).evaluate(patient));
    }
}