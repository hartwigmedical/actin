package com.hartwig.actin.algo.evaluation.composite;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.TestEvaluationFunctionFactory;

import org.junit.Test;

public class WarnOnPassTest {

    @Test
    public void canWarnOnPass() {
        PatientRecord patient = TestDataFactory.createProperTestPatientRecord();

        assertEquals(EvaluationResult.PASS_BUT_WARN, new WarnOnPass(TestEvaluationFunctionFactory.pass()).evaluate(patient));
        assertEquals(EvaluationResult.PASS_BUT_WARN, new WarnOnPass(TestEvaluationFunctionFactory.passButWarn()).evaluate(patient));
        assertEquals(EvaluationResult.PASS, new WarnOnPass(TestEvaluationFunctionFactory.fail()).evaluate(patient));
        assertEquals(EvaluationResult.UNDETERMINED, new WarnOnPass(TestEvaluationFunctionFactory.undetermined()).evaluate(patient));
        assertEquals(EvaluationResult.NOT_IMPLEMENTED, new WarnOnPass(TestEvaluationFunctionFactory.notImplemented()).evaluate(patient));
        assertEquals(EvaluationResult.NOT_EVALUATED, new WarnOnPass(TestEvaluationFunctionFactory.notEvaluated()).evaluate(patient));
    }
}