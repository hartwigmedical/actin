package com.hartwig.actin.algo.evaluation.composite;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.TestEvaluationFunctionFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class AndTest {

    private static final PatientRecord TEST_PATIENT = TestDataFactory.createProperTestPatientRecord();

    @Test
    public void canCombineEvaluations() {
        assertEquals(Evaluation.IGNORED, combinedWithIgnored(TestEvaluationFunctionFactory.ignored()));
        assertEquals(Evaluation.PASS, combinedWithIgnored(TestEvaluationFunctionFactory.pass()));
        assertEquals(Evaluation.PASS_BUT_WARN, combinedWithIgnored(TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(Evaluation.NOT_IMPLEMENTED, combinedWithIgnored(TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(Evaluation.UNDETERMINED, combinedWithIgnored(TestEvaluationFunctionFactory.undetermined()));
        assertEquals(Evaluation.FAIL, combinedWithIgnored(TestEvaluationFunctionFactory.fail()));

        assertEquals(Evaluation.PASS, combinedWithPass(TestEvaluationFunctionFactory.ignored()));
        assertEquals(Evaluation.PASS, combinedWithPass(TestEvaluationFunctionFactory.pass()));
        assertEquals(Evaluation.PASS_BUT_WARN, combinedWithPass(TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(Evaluation.NOT_IMPLEMENTED, combinedWithPass(TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(Evaluation.UNDETERMINED, combinedWithPass(TestEvaluationFunctionFactory.undetermined()));
        assertEquals(Evaluation.FAIL, combinedWithPass(TestEvaluationFunctionFactory.fail()));

        assertEquals(Evaluation.PASS_BUT_WARN, combinedWithPassButWarn(TestEvaluationFunctionFactory.ignored()));
        assertEquals(Evaluation.PASS_BUT_WARN, combinedWithPassButWarn(TestEvaluationFunctionFactory.pass()));
        assertEquals(Evaluation.PASS_BUT_WARN, combinedWithPassButWarn(TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(Evaluation.NOT_IMPLEMENTED, combinedWithPassButWarn(TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(Evaluation.UNDETERMINED, combinedWithPassButWarn(TestEvaluationFunctionFactory.undetermined()));
        assertEquals(Evaluation.FAIL, combinedWithPassButWarn(TestEvaluationFunctionFactory.fail()));

        assertEquals(Evaluation.NOT_IMPLEMENTED, combinedWithNotImplemented(TestEvaluationFunctionFactory.ignored()));
        assertEquals(Evaluation.NOT_IMPLEMENTED, combinedWithNotImplemented(TestEvaluationFunctionFactory.pass()));
        assertEquals(Evaluation.NOT_IMPLEMENTED, combinedWithNotImplemented(TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(Evaluation.NOT_IMPLEMENTED, combinedWithNotImplemented(TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(Evaluation.UNDETERMINED, combinedWithNotImplemented(TestEvaluationFunctionFactory.undetermined()));
        assertEquals(Evaluation.FAIL, combinedWithNotImplemented(TestEvaluationFunctionFactory.fail()));

        assertEquals(Evaluation.UNDETERMINED, combinedWithUndetermined(TestEvaluationFunctionFactory.ignored()));
        assertEquals(Evaluation.UNDETERMINED, combinedWithUndetermined(TestEvaluationFunctionFactory.pass()));
        assertEquals(Evaluation.UNDETERMINED, combinedWithUndetermined(TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(Evaluation.UNDETERMINED, combinedWithUndetermined(TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(Evaluation.UNDETERMINED, combinedWithUndetermined(TestEvaluationFunctionFactory.undetermined()));
        assertEquals(Evaluation.FAIL, combinedWithUndetermined(TestEvaluationFunctionFactory.fail()));

        assertEquals(Evaluation.FAIL, combinedWithFail(TestEvaluationFunctionFactory.ignored()));
        assertEquals(Evaluation.FAIL, combinedWithFail(TestEvaluationFunctionFactory.pass()));
        assertEquals(Evaluation.FAIL, combinedWithFail(TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(Evaluation.FAIL, combinedWithFail(TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(Evaluation.FAIL, combinedWithFail(TestEvaluationFunctionFactory.undetermined()));
        assertEquals(Evaluation.FAIL, combinedWithFail(TestEvaluationFunctionFactory.fail()));
    }

    @Test(expected = IllegalStateException.class)
    public void crashOnNoFunctionsToEvaluate() {
        new And(Lists.newArrayList()).evaluate(TEST_PATIENT);
    }

    @NotNull
    private static Evaluation combinedWithPass(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.pass(), function);
    }

    @NotNull
    private static Evaluation combinedWithPassButWarn(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.passButWarn(), function);
    }

    @NotNull
    private static Evaluation combinedWithFail(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.fail(), function);
    }

    @NotNull
    private static Evaluation combinedWithUndetermined(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.undetermined(), function);
    }

    @NotNull
    private static Evaluation combinedWithIgnored(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.ignored(), function);
    }

    @NotNull
    private static Evaluation combinedWithNotImplemented(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.notImplemented(), function);
    }

    @NotNull
    private static Evaluation evaluate(@NotNull EvaluationFunction function1, @NotNull EvaluationFunction function2) {
        return new And(Lists.newArrayList(function1, function2)).evaluate(TEST_PATIENT);
    }
}