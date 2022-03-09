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

public class WarnIfTest {

    @Test
    public void canWarnIf() {
        PatientRecord patient = TestDataFactory.createProperTestPatientRecord();

        assertEvaluation(EvaluationResult.PASS_BUT_WARN, new WarnIf(TestEvaluationFunctionFactory.pass()).evaluate(patient));
        assertEvaluation(EvaluationResult.PASS_BUT_WARN, new WarnIf(TestEvaluationFunctionFactory.passButWarn()).evaluate(patient));
        assertEvaluation(EvaluationResult.PASS, new WarnIf(TestEvaluationFunctionFactory.fail()).evaluate(patient));
        assertEvaluation(EvaluationResult.UNDETERMINED, new WarnIf(TestEvaluationFunctionFactory.undetermined()).evaluate(patient));
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED,
                new WarnIf(TestEvaluationFunctionFactory.notImplemented()).evaluate(patient));
        assertEvaluation(EvaluationResult.NOT_EVALUATED, new WarnIf(TestEvaluationFunctionFactory.notEvaluated()).evaluate(patient));
    }

    @Test
    public void canFlipMessagesOnFail() {
        EvaluationFunction fail = record -> ImmutableEvaluation.builder()
                .result(EvaluationResult.FAIL)
                .addFailMessages("fail 1")
                .addUndeterminedMessages("undetermined 1")
                .addPassMessages("pass 1")
                .build();

        Evaluation result = new WarnIf(fail).evaluate(TestDataFactory.createMinimalTestPatientRecord());

        assertTrue(result.passMessages().contains("fail 1"));
        assertTrue(result.failMessages().isEmpty());
    }
}