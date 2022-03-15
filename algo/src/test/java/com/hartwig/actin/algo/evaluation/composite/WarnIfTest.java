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

        assertEvaluation(EvaluationResult.WARN, new WarnIf(TestEvaluationFunctionFactory.pass()).evaluate(patient));
        assertEvaluation(EvaluationResult.WARN, new WarnIf(TestEvaluationFunctionFactory.warn()).evaluate(patient));
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
                .addFailSpecificMessages("fail 1")
                .addUndeterminedSpecificMessages("undetermined 1")
                .addPassSpecificMessages("pass 1")
                .build();

        Evaluation result = new WarnIf(fail).evaluate(TestDataFactory.createMinimalTestPatientRecord());

        assertTrue(result.passSpecificMessages().contains("fail 1"));
        assertTrue(result.failSpecificMessages().isEmpty());
    }
}