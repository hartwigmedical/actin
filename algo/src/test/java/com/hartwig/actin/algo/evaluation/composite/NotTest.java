package com.hartwig.actin.algo.evaluation.composite;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.TestEvaluationFunctionFactory;

import org.junit.Test;

public class NotTest {

    @Test
    public void canNegateEvaluation() {
        PatientRecord patient = TestDataFactory.createTestPatientRecord();

        assertEquals(Evaluation.FAIL, new Not(TestEvaluationFunctionFactory.pass()).evaluate(patient));
        assertEquals(Evaluation.FAIL, new Not(TestEvaluationFunctionFactory.passButWarn()).evaluate(patient));
        assertEquals(Evaluation.PASS, new Not(TestEvaluationFunctionFactory.fail()).evaluate(patient));
        assertEquals(Evaluation.UNDETERMINED, new Not(TestEvaluationFunctionFactory.undetermined()).evaluate(patient));
    }
}