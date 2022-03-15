package com.hartwig.actin.algo.evaluation.composite;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import static org.junit.Assert.assertTrue;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.TestEvaluationFunctionFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class WarnIfTest {

    @Test
    public void canWarnIf() {
        PatientRecord patient = TestDataFactory.createProperTestPatientRecord();

        assertEvaluation(EvaluationResult.WARN, new WarnIf(TestEvaluationFunctionFactory.pass()).evaluate(patient));
        assertEvaluation(EvaluationResult.PASS, new WarnIf(TestEvaluationFunctionFactory.warn()).evaluate(patient));
        assertEvaluation(EvaluationResult.PASS, new WarnIf(TestEvaluationFunctionFactory.fail()).evaluate(patient));
        assertEvaluation(EvaluationResult.PASS, new WarnIf(TestEvaluationFunctionFactory.undetermined()).evaluate(patient));
        assertEvaluation(EvaluationResult.PASS, new WarnIf(TestEvaluationFunctionFactory.notImplemented()).evaluate(patient));
        assertEvaluation(EvaluationResult.PASS, new WarnIf(TestEvaluationFunctionFactory.notEvaluated()).evaluate(patient));
    }

    @Test
    public void canMoveMessagesToWarnOnPass() {
        Evaluation result = new WarnIf(TestEvaluationFunctionFactory.pass()).evaluate(TestDataFactory.createMinimalTestPatientRecord());

        assertTrue(result.passSpecificMessages().isEmpty());
        assertTrue(result.passGeneralMessages().isEmpty());
        assertTrue(result.warnSpecificMessages().contains("pass specific"));
        assertTrue(result.warnGeneralMessages().contains("pass general"));
    }

    @NotNull
    private static Evaluation create(@NotNull EvaluationResult result) {
        return ImmutableEvaluation.builder()
                .result(result)
                .addPassSpecificMessages("pass specific")
                .addPassGeneralMessages("pass general")
                .addWarnSpecificMessages("warn specific")
                .addWarnGeneralMessages("warn general")
                .addUndeterminedSpecificMessages("undetermined specific")
                .addUndeterminedGeneralMessages("undetermined general")
                .addFailSpecificMessages("fail specific")
                .addFailGeneralMessages("fail general")
                .build();
    }
}