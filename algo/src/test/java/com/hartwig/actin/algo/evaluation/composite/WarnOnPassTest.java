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

public class WarnOnPassTest {

    @Test
    public void canWarnOnPass() {
        PatientRecord patient = TestDataFactory.createProperTestPatientRecord();

        assertEvaluation(EvaluationResult.PASS_BUT_WARN, new WarnOnPass(TestEvaluationFunctionFactory.pass()).evaluate(patient));
        assertEvaluation(EvaluationResult.PASS_BUT_WARN, new WarnOnPass(TestEvaluationFunctionFactory.passButWarn()).evaluate(patient));
        assertEvaluation(EvaluationResult.PASS, new WarnOnPass(TestEvaluationFunctionFactory.fail()).evaluate(patient));
        assertEvaluation(EvaluationResult.UNDETERMINED, new WarnOnPass(TestEvaluationFunctionFactory.undetermined()).evaluate(patient));
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED,
                new WarnOnPass(TestEvaluationFunctionFactory.notImplemented()).evaluate(patient));
        assertEvaluation(EvaluationResult.NOT_EVALUATED, new WarnOnPass(TestEvaluationFunctionFactory.notEvaluated()).evaluate(patient));
    }

    @Test
    public void canFlipMessagesOnFail() {
        EvaluationFunction fail = record -> ImmutableEvaluation.builder()
                .result(EvaluationResult.FAIL)
                .addFailMessages("fail 1")
                .addUndeterminedMessages("undetermined 1")
                .addPassMessages("pass 1")
                .build();

        Evaluation result = new WarnOnPass(fail).evaluate(TestDataFactory.createMinimalTestPatientRecord());

        assertTrue(result.passMessages().contains("fail 1"));
        assertTrue(result.failMessages().isEmpty());
    }
}