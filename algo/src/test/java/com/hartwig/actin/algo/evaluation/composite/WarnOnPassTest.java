package com.hartwig.actin.algo.evaluation.composite;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.TestEvaluationFunctionFactory;

import org.junit.Test;

public class WarnOnPassTest {

    @Test
    public void canWarnOnPass() {
        PatientRecord patient = TestDataFactory.createProperTestPatientRecord();

        assertEquals(Evaluation.PASS_BUT_WARN, new WarnOnPass(TestEvaluationFunctionFactory.pass()).evaluate(patient));
        assertEquals(Evaluation.PASS_BUT_WARN, new WarnOnPass(TestEvaluationFunctionFactory.passButWarn()).evaluate(patient));
        assertEquals(Evaluation.PASS, new WarnOnPass(TestEvaluationFunctionFactory.fail()).evaluate(patient));
        assertEquals(Evaluation.UNDETERMINED, new WarnOnPass(TestEvaluationFunctionFactory.undetermined()).evaluate(patient));
        assertEquals(Evaluation.NOT_IMPLEMENTED, new WarnOnPass(TestEvaluationFunctionFactory.notImplemented()).evaluate(patient));
        assertEquals(Evaluation.NOT_EVALUATED, new WarnOnPass(TestEvaluationFunctionFactory.notEvaluated()).evaluate(patient));
    }
}