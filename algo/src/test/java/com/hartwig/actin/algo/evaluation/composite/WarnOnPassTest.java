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

        assertEquals(EvaluationResult.PASS_BUT_WARN, new WarnOnPass(TestEvaluationFunctionFactory.pass()).evaluate(patient).result());
        assertEquals(EvaluationResult.PASS_BUT_WARN,
                new WarnOnPass(TestEvaluationFunctionFactory.passButWarn()).evaluate(patient).result());
        assertEquals(EvaluationResult.PASS, new WarnOnPass(TestEvaluationFunctionFactory.fail()).evaluate(patient).result());
        assertEquals(EvaluationResult.UNDETERMINED,
                new WarnOnPass(TestEvaluationFunctionFactory.undetermined()).evaluate(patient).result());
        assertEquals(EvaluationResult.NOT_IMPLEMENTED,
                new WarnOnPass(TestEvaluationFunctionFactory.notImplemented()).evaluate(patient).result());
        assertEquals(EvaluationResult.NOT_EVALUATED,
                new WarnOnPass(TestEvaluationFunctionFactory.notEvaluated()).evaluate(patient).result());
    }
}