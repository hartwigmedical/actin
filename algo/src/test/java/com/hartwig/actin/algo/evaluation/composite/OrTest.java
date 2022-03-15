package com.hartwig.actin.algo.evaluation.composite;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.TestEvaluationFunctionFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class OrTest {

    private static final PatientRecord TEST_PATIENT = TestDataFactory.createProperTestPatientRecord();

    @Test
    public void canCombineEvaluations() {
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.pass()));
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.notEvaluated()));
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.warn()));
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.undetermined()));
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.fail()));
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.notImplemented()));

        assertEvaluation(EvaluationResult.PASS, combineWithNotEvaluated(TestEvaluationFunctionFactory.pass()));
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.notEvaluated()));
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.warn()));
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.undetermined()));
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.fail()));
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.notImplemented()));

        assertEvaluation(EvaluationResult.PASS, combineWithWarn(TestEvaluationFunctionFactory.pass()));
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithWarn(TestEvaluationFunctionFactory.notEvaluated()));
        assertEvaluation(EvaluationResult.WARN, combineWithWarn(TestEvaluationFunctionFactory.warn()));
        assertEvaluation(EvaluationResult.WARN, combineWithWarn(TestEvaluationFunctionFactory.undetermined()));
        assertEvaluation(EvaluationResult.WARN, combineWithWarn(TestEvaluationFunctionFactory.fail()));
        assertEvaluation(EvaluationResult.WARN, combineWithWarn(TestEvaluationFunctionFactory.notImplemented()));

        assertEvaluation(EvaluationResult.PASS, combineWithUndetermined(TestEvaluationFunctionFactory.pass()));
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithUndetermined(TestEvaluationFunctionFactory.notEvaluated()));
        assertEvaluation(EvaluationResult.WARN, combineWithUndetermined(TestEvaluationFunctionFactory.warn()));
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.undetermined()));
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.fail()));
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.notImplemented()));

        assertEvaluation(EvaluationResult.PASS, combineWithFail(TestEvaluationFunctionFactory.pass()));
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithFail(TestEvaluationFunctionFactory.notEvaluated()));
        assertEvaluation(EvaluationResult.WARN, combineWithFail(TestEvaluationFunctionFactory.warn()));
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithFail(TestEvaluationFunctionFactory.undetermined()));
        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.fail()));
        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.notImplemented()));

        assertEvaluation(EvaluationResult.PASS, combineWithNotImplemented(TestEvaluationFunctionFactory.pass()));
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotImplemented(TestEvaluationFunctionFactory.notEvaluated()));
        assertEvaluation(EvaluationResult.WARN, combineWithNotImplemented(TestEvaluationFunctionFactory.warn()));
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithNotImplemented(TestEvaluationFunctionFactory.undetermined()));
        assertEvaluation(EvaluationResult.FAIL, combineWithNotImplemented(TestEvaluationFunctionFactory.fail()));
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithNotImplemented(TestEvaluationFunctionFactory.notImplemented()));

    }

    @Test
    public void canRetainMessages() {
        EvaluationFunction  function1 = record -> ImmutableEvaluation.builder()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("fail 1")
                .addUndeterminedSpecificMessages("undetermined 1")
                .addPassSpecificMessages("pass 1")
                .build();

        EvaluationFunction function2 = record -> ImmutableEvaluation.builder()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("fail 2")
                .addUndeterminedSpecificMessages("undetermined 2")
                .addPassSpecificMessages("pass 2")
                .build();

        EvaluationFunction function3 = record -> ImmutableEvaluation.builder()
                .result(EvaluationResult.PASS)
                .addFailSpecificMessages("fail 3")
                .addUndeterminedSpecificMessages("undetermined 3")
                .addPassSpecificMessages("pass 3")
                .build();

        EvaluationFunction function4 = record -> ImmutableEvaluation.builder()
                .result(EvaluationResult.PASS)
                .addFailSpecificMessages("fail 4")
                .addUndeterminedSpecificMessages("undetermined 4")
                .addPassSpecificMessages("pass 4")
                .build();

        Evaluation result = new Or(Lists.newArrayList(function1, function2, function3, function4)).evaluate(TEST_PATIENT);
        assertEquals(2, result.failSpecificMessages().size());
        assertTrue(result.failSpecificMessages().contains("fail 3"));
        assertTrue(result.failSpecificMessages().contains("fail 4"));

        assertEquals(2, result.passSpecificMessages().size());
        assertTrue(result.passSpecificMessages().contains("pass 3"));
        assertTrue(result.passSpecificMessages().contains("pass 4"));

        assertEquals(2, result.undeterminedSpecificMessages().size());
        assertTrue(result.undeterminedSpecificMessages().contains("undetermined 3"));
        assertTrue(result.undeterminedSpecificMessages().contains("undetermined 4"));
    }

    @Test(expected = IllegalStateException.class)
    public void crashOnNoFunctionsToEvaluate() {
        new Or(Lists.newArrayList()).evaluate(TEST_PATIENT);
    }

    @NotNull
    private static Evaluation combineWithPass(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.pass(), function);
    }

    @NotNull
    private static Evaluation combineWithWarn(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.warn(), function);
    }

    @NotNull
    private static Evaluation combineWithFail(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.fail(), function);
    }

    @NotNull
    private static Evaluation combineWithUndetermined(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.undetermined(), function);
    }

    @NotNull
    private static Evaluation combineWithNotEvaluated(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.notEvaluated(), function);
    }

    @NotNull
    private static Evaluation combineWithNotImplemented(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.notImplemented(), function);
    }

    @NotNull
    private static Evaluation evaluate(@NotNull EvaluationFunction function1, @NotNull EvaluationFunction function2) {
        return new Or(Lists.newArrayList(function1, function2)).evaluate(TEST_PATIENT);
    }
}