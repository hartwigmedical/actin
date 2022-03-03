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

public class AndTest {

    private static final PatientRecord TEST_PATIENT = TestDataFactory.createProperTestPatientRecord();

    @Test
    public void canCombineEvaluations() {
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.pass()));
        assertEvaluation(EvaluationResult.PASS_BUT_WARN, combineWithPass(TestEvaluationFunctionFactory.passButWarn()));
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithPass(TestEvaluationFunctionFactory.notEvaluated()));
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithPass(TestEvaluationFunctionFactory.notImplemented()));
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithPass(TestEvaluationFunctionFactory.undetermined()));
        assertEvaluation(EvaluationResult.FAIL, combineWithPass(TestEvaluationFunctionFactory.fail()));

        assertEvaluation(EvaluationResult.PASS_BUT_WARN, combineWithPassButWarn(TestEvaluationFunctionFactory.pass()));
        assertEvaluation(EvaluationResult.PASS_BUT_WARN, combineWithPassButWarn(TestEvaluationFunctionFactory.passButWarn()));
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithPassButWarn(TestEvaluationFunctionFactory.notEvaluated()));
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithPassButWarn(TestEvaluationFunctionFactory.notImplemented()));
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithPassButWarn(TestEvaluationFunctionFactory.undetermined()));
        assertEvaluation(EvaluationResult.FAIL, combineWithPassButWarn(TestEvaluationFunctionFactory.fail()));

        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.pass()));
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.passButWarn()));
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.notEvaluated()));
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithNotEvaluated(TestEvaluationFunctionFactory.notImplemented()));
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithNotEvaluated(TestEvaluationFunctionFactory.undetermined()));
        assertEvaluation(EvaluationResult.FAIL, combineWithNotEvaluated(TestEvaluationFunctionFactory.fail()));

        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithNotImplemented(TestEvaluationFunctionFactory.pass()));
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithNotImplemented(TestEvaluationFunctionFactory.passButWarn()));
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithNotImplemented(TestEvaluationFunctionFactory.notEvaluated()));
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithNotImplemented(TestEvaluationFunctionFactory.notImplemented()));
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithNotImplemented(TestEvaluationFunctionFactory.undetermined()));
        assertEvaluation(EvaluationResult.FAIL, combineWithNotImplemented(TestEvaluationFunctionFactory.fail()));

        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.pass()));
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.passButWarn()));
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.notEvaluated()));
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.notImplemented()));
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.undetermined()));
        assertEvaluation(EvaluationResult.FAIL, combineWithUndetermined(TestEvaluationFunctionFactory.fail()));

        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.pass()));
        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.passButWarn()));
        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.notEvaluated()));
        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.notImplemented()));
        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.undetermined()));
        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.fail()));
    }

    @Test
    public void canRetainMessages() {
        EvaluationFunction  function1 = record -> ImmutableEvaluation.builder()
                .result(EvaluationResult.FAIL)
                .addFailMessages("fail 1")
                .addUndeterminedMessages("undetermined 1")
                .addPassMessages("pass 1")
                .build();

        EvaluationFunction function2 = record -> ImmutableEvaluation.builder()
                .result(EvaluationResult.FAIL)
                .addFailMessages("fail 2")
                .addUndeterminedMessages("undetermined 2")
                .addPassMessages("pass 2")
                .build();

        EvaluationFunction function3 = record -> ImmutableEvaluation.builder()
                .result(EvaluationResult.PASS)
                .addFailMessages("fail 3")
                .addUndeterminedMessages("undetermined 3")
                .addPassMessages("pass 3")
                .build();

        EvaluationFunction function4 = record -> ImmutableEvaluation.builder()
                .result(EvaluationResult.PASS)
                .addFailMessages("fail 4")
                .addUndeterminedMessages("undetermined 4")
                .addPassMessages("pass 4")
                .build();

        Evaluation result = new And(Lists.newArrayList(function1, function2, function3, function4)).evaluate(TEST_PATIENT);
        assertEquals(2, result.failMessages().size());
        assertTrue(result.failMessages().contains("fail 1"));
        assertTrue(result.failMessages().contains("fail 2"));

        assertEquals(2, result.passMessages().size());
        assertTrue(result.passMessages().contains("pass 1"));
        assertTrue(result.passMessages().contains("pass 2"));

        assertEquals(2, result.undeterminedMessages().size());
        assertTrue(result.undeterminedMessages().contains("undetermined 1"));
        assertTrue(result.undeterminedMessages().contains("undetermined 2"));
    }

    @Test(expected = IllegalStateException.class)
    public void crashOnNoFunctionsToEvaluate() {
        new And(Lists.newArrayList()).evaluate(TEST_PATIENT);
    }

    @NotNull
    private static Evaluation combineWithPass(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.pass(), function);
    }

    @NotNull
    private static Evaluation combineWithPassButWarn(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.passButWarn(), function);
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
        return new And(Lists.newArrayList(function1, function2)).evaluate(TEST_PATIENT);
    }
}