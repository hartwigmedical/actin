package com.hartwig.actin.algo.evaluation.composite;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.TestEvaluationFunctionFactory;

import org.junit.Test;

public class NotTest {

    private static final PatientRecord TEST_PATIENT = TestDataFactory.createProperTestPatientRecord();

    @Test
    public void canNegateEvaluation() {
        assertEvaluation(EvaluationResult.FAIL, new Not(TestEvaluationFunctionFactory.pass()).evaluate(TEST_PATIENT));
        assertEvaluation(EvaluationResult.PASS, new Not(TestEvaluationFunctionFactory.fail()).evaluate(TEST_PATIENT));
        assertEvaluation(EvaluationResult.WARN, new Not(TestEvaluationFunctionFactory.warn()).evaluate(TEST_PATIENT));
        assertEvaluation(EvaluationResult.UNDETERMINED, new Not(TestEvaluationFunctionFactory.undetermined()).evaluate(TEST_PATIENT));
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, new Not(TestEvaluationFunctionFactory.notImplemented()).evaluate(TEST_PATIENT));
        assertEvaluation(EvaluationResult.NOT_EVALUATED, new Not(TestEvaluationFunctionFactory.notEvaluated()).evaluate(TEST_PATIENT));
    }

    @Test
    public void canFlipMessagesForPass() {
        Evaluation passed = CompositeTestFactory.create(EvaluationResult.PASS);

        Evaluation result = new Not(record -> passed).evaluate(TEST_PATIENT);

        assertEquals(passed.passSpecificMessages(), result.failSpecificMessages());
        assertEquals(passed.passGeneralMessages(), result.failGeneralMessages());
        assertEquals(passed.failSpecificMessages(), result.passSpecificMessages());
        assertEquals(passed.failGeneralMessages(), result.passGeneralMessages());

        assertEquals(passed.undeterminedSpecificMessages(), result.undeterminedSpecificMessages());
        assertEquals(passed.undeterminedGeneralMessages(), result.undeterminedGeneralMessages());
        assertEquals(passed.warnSpecificMessages(), result.warnSpecificMessages());
        assertEquals(passed.warnGeneralMessages(), result.warnGeneralMessages());
    }

    @Test
    public void canFlipMessagesForFail() {
        Evaluation failed = CompositeTestFactory.create(EvaluationResult.FAIL);

        Evaluation result = new Not(record -> failed).evaluate(TEST_PATIENT);

        assertEquals(failed.passSpecificMessages(), result.failSpecificMessages());
        assertEquals(failed.passGeneralMessages(), result.failGeneralMessages());
        assertEquals(failed.failSpecificMessages(), result.passSpecificMessages());
        assertEquals(failed.failGeneralMessages(), result.passGeneralMessages());

        assertEquals(failed.undeterminedSpecificMessages(), result.undeterminedSpecificMessages());
        assertEquals(failed.undeterminedGeneralMessages(), result.undeterminedGeneralMessages());
        assertEquals(failed.warnSpecificMessages(), result.warnSpecificMessages());
        assertEquals(failed.warnGeneralMessages(), result.warnGeneralMessages());
    }

    @Test
    public void canRetainMessagesForUndetermined() {
        Evaluation undetermined = CompositeTestFactory.create(EvaluationResult.UNDETERMINED);

        Evaluation result = new Not(record -> undetermined).evaluate(TEST_PATIENT);

        assertEquals(undetermined.passSpecificMessages(), result.passSpecificMessages());
        assertEquals(undetermined.passGeneralMessages(), result.passGeneralMessages());
        assertEquals(undetermined.failSpecificMessages(), result.failSpecificMessages());
        assertEquals(undetermined.failGeneralMessages(), result.failGeneralMessages());

        assertEquals(undetermined.undeterminedSpecificMessages(), result.undeterminedSpecificMessages());
        assertEquals(undetermined.undeterminedGeneralMessages(), result.undeterminedGeneralMessages());
        assertEquals(undetermined.warnSpecificMessages(), result.warnSpecificMessages());
        assertEquals(undetermined.warnGeneralMessages(), result.warnGeneralMessages());
    }
}