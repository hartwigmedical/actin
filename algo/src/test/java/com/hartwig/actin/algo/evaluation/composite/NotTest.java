package com.hartwig.actin.algo.evaluation.composite;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import static org.junit.Assert.assertTrue;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.TestEvaluationFunctionFactory;

import org.junit.Test;

public class NotTest {

    private static final PatientRecord TEST_PATIENT = TestDataFactory.createProperTestPatientRecord();

    @Test
    public void canNegateEvaluation() {
        assertEvaluation(EvaluationResult.FAIL, new Not(TestEvaluationFunctionFactory.pass()).evaluate(TEST_PATIENT));
        assertEvaluation(EvaluationResult.FAIL, new Not(TestEvaluationFunctionFactory.warn()).evaluate(TEST_PATIENT));
        assertEvaluation(EvaluationResult.PASS, new Not(TestEvaluationFunctionFactory.fail()).evaluate(TEST_PATIENT));
        assertEvaluation(EvaluationResult.UNDETERMINED, new Not(TestEvaluationFunctionFactory.undetermined()).evaluate(TEST_PATIENT));
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, new Not(TestEvaluationFunctionFactory.notImplemented()).evaluate(TEST_PATIENT));
        assertEvaluation(EvaluationResult.NOT_EVALUATED, new Not(TestEvaluationFunctionFactory.notEvaluated()).evaluate(TEST_PATIENT));
    }

    @Test
    public void canFlipMessagesForPass() {
        EvaluationFunction pass = record -> ImmutableEvaluation.builder()
                .result(EvaluationResult.PASS)
                .addPassSpecificMessages("pass")
                .addUndeterminedSpecificMessages("undetermined")
                .addFailSpecificMessages("fail")
                .build();

        Evaluation result = new Not(pass).evaluate(TEST_PATIENT);

        assertTrue(result.failSpecificMessages().contains("pass"));
        assertTrue(result.passSpecificMessages().contains("fail"));
        assertTrue(result.undeterminedSpecificMessages().contains("undetermined"));
    }

    @Test
    public void canFlipMessagesForFail() {
        EvaluationFunction fail = record -> ImmutableEvaluation.builder()
                .result(EvaluationResult.FAIL)
                .addPassSpecificMessages("pass")
                .addUndeterminedSpecificMessages("undetermined")
                .addFailSpecificMessages("fail")
                .build();

        Evaluation result = new Not(fail).evaluate(TEST_PATIENT);

        assertTrue(result.failSpecificMessages().contains("pass"));
        assertTrue(result.passSpecificMessages().contains("fail"));
        assertTrue(result.undeterminedSpecificMessages().contains("undetermined"));
    }

    @Test
    public void canRetainMessagesForUndetermined() {
        EvaluationFunction fail = record -> ImmutableEvaluation.builder()
                .result(EvaluationResult.UNDETERMINED)
                .addPassSpecificMessages("pass")
                .addUndeterminedSpecificMessages("undetermined")
                .addFailSpecificMessages("fail")
                .build();

        Evaluation result = new Not(fail).evaluate(TEST_PATIENT);

        assertTrue(result.failSpecificMessages().contains("fail"));
        assertTrue(result.passSpecificMessages().contains("pass"));
        assertTrue(result.undeterminedSpecificMessages().contains("undetermined"));
    }
}