package com.hartwig.actin.algo.evaluation.composite;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.TestEvaluationFunctionFactory;

import org.junit.Test;

public class NotTest {

    @Test
    public void canNegateEvaluation() {
        PatientRecord patient = TestDataFactory.createProperTestPatientRecord();

        assertEquals(EvaluationResult.FAIL, new Not(TestEvaluationFunctionFactory.pass()).evaluate(patient).result());
        assertEquals(EvaluationResult.FAIL, new Not(TestEvaluationFunctionFactory.passButWarn()).evaluate(patient).result());
        assertEquals(EvaluationResult.PASS, new Not(TestEvaluationFunctionFactory.fail()).evaluate(patient).result());
        assertEquals(EvaluationResult.UNDETERMINED, new Not(TestEvaluationFunctionFactory.undetermined()).evaluate(patient).result());
        assertEquals(EvaluationResult.NOT_IMPLEMENTED, new Not(TestEvaluationFunctionFactory.notImplemented()).evaluate(patient).result());
        assertEquals(EvaluationResult.NOT_EVALUATED, new Not(TestEvaluationFunctionFactory.notEvaluated()).evaluate(patient).result());
    }
}