package com.hartwig.actin.algo.evaluation.composite;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.TestEvaluationFunctionFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class OrTest {

    private static final PatientRecord TEST_PATIENT = TestDataFactory.createProperTestPatientRecord();

    @Test
    public void canCombineEvaluations() {
        assertEquals(EvaluationResult.PASS, combinedWithPass(TestEvaluationFunctionFactory.pass()));
        assertEquals(EvaluationResult.PASS, combinedWithPass(TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(EvaluationResult.PASS, combinedWithPass(TestEvaluationFunctionFactory.notEvaluated()));
        assertEquals(EvaluationResult.PASS, combinedWithPass(TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(EvaluationResult.PASS, combinedWithPass(TestEvaluationFunctionFactory.undetermined()));
        assertEquals(EvaluationResult.PASS, combinedWithPass(TestEvaluationFunctionFactory.fail()));

        assertEquals(EvaluationResult.PASS, combinedWithPassButWarn(TestEvaluationFunctionFactory.pass()));
        assertEquals(EvaluationResult.PASS_BUT_WARN, combinedWithPassButWarn(TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(EvaluationResult.PASS_BUT_WARN, combinedWithPassButWarn(TestEvaluationFunctionFactory.notEvaluated()));
        assertEquals(EvaluationResult.PASS_BUT_WARN, combinedWithPassButWarn(TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(EvaluationResult.PASS_BUT_WARN, combinedWithPassButWarn(TestEvaluationFunctionFactory.undetermined()));
        assertEquals(EvaluationResult.PASS_BUT_WARN, combinedWithPassButWarn(TestEvaluationFunctionFactory.fail()));

        assertEquals(EvaluationResult.PASS, combinedWithNotEvaluated(TestEvaluationFunctionFactory.pass()));
        assertEquals(EvaluationResult.PASS_BUT_WARN, combinedWithNotEvaluated(TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(EvaluationResult.NOT_EVALUATED, combinedWithNotEvaluated(TestEvaluationFunctionFactory.notEvaluated()));
        assertEquals(EvaluationResult.NOT_EVALUATED, combinedWithNotEvaluated(TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(EvaluationResult.NOT_EVALUATED, combinedWithNotEvaluated(TestEvaluationFunctionFactory.undetermined()));
        assertEquals(EvaluationResult.NOT_EVALUATED, combinedWithNotEvaluated(TestEvaluationFunctionFactory.fail()));

        assertEquals(EvaluationResult.PASS, combinedWithNotImplemented(TestEvaluationFunctionFactory.pass()));
        assertEquals(EvaluationResult.PASS_BUT_WARN, combinedWithNotImplemented(TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(EvaluationResult.NOT_EVALUATED, combinedWithNotImplemented(TestEvaluationFunctionFactory.notEvaluated()));
        assertEquals(EvaluationResult.NOT_IMPLEMENTED, combinedWithNotImplemented(TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(EvaluationResult.NOT_IMPLEMENTED, combinedWithNotImplemented(TestEvaluationFunctionFactory.undetermined()));
        assertEquals(EvaluationResult.NOT_IMPLEMENTED, combinedWithNotImplemented(TestEvaluationFunctionFactory.fail()));

        assertEquals(EvaluationResult.PASS, combinedWithUndetermined(TestEvaluationFunctionFactory.pass()));
        assertEquals(EvaluationResult.PASS_BUT_WARN, combinedWithUndetermined(TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(EvaluationResult.NOT_EVALUATED, combinedWithUndetermined(TestEvaluationFunctionFactory.notEvaluated()));
        assertEquals(EvaluationResult.NOT_IMPLEMENTED, combinedWithUndetermined(TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(EvaluationResult.UNDETERMINED, combinedWithUndetermined(TestEvaluationFunctionFactory.undetermined()));
        assertEquals(EvaluationResult.UNDETERMINED, combinedWithUndetermined(TestEvaluationFunctionFactory.fail()));

        assertEquals(EvaluationResult.PASS, combinedWithFail(TestEvaluationFunctionFactory.pass()));
        assertEquals(EvaluationResult.PASS_BUT_WARN, combinedWithFail(TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(EvaluationResult.NOT_EVALUATED, combinedWithFail(TestEvaluationFunctionFactory.notEvaluated()));
        assertEquals(EvaluationResult.NOT_IMPLEMENTED, combinedWithFail(TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(EvaluationResult.UNDETERMINED, combinedWithFail(TestEvaluationFunctionFactory.undetermined()));
        assertEquals(EvaluationResult.FAIL, combinedWithFail(TestEvaluationFunctionFactory.fail()));
    }

    @Test(expected = IllegalStateException.class)
    public void crashOnNoFunctionsToEvaluate() {
        new Or(Lists.newArrayList()).evaluate(TEST_PATIENT);
    }

    @NotNull
    private static EvaluationResult combinedWithPass(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.pass(), function);
    }

    @NotNull
    private static EvaluationResult combinedWithPassButWarn(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.passButWarn(), function);
    }

    @NotNull
    private static EvaluationResult combinedWithFail(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.fail(), function);
    }

    @NotNull
    private static EvaluationResult combinedWithUndetermined(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.undetermined(), function);
    }

    @NotNull
    private static EvaluationResult combinedWithNotEvaluated(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.notEvaluated(), function);
    }

    @NotNull
    private static EvaluationResult combinedWithNotImplemented(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.notImplemented(), function);
    }

    @NotNull
    private static EvaluationResult evaluate(@NotNull EvaluationFunction function1, @NotNull EvaluationFunction function2) {
        return new Or(Lists.newArrayList(function1, function2)).evaluate(TEST_PATIENT).result();
    }
}