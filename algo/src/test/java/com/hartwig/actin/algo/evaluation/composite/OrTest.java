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

public class OrTest {

    private static final PatientRecord TEST_PATIENT = TestDataFactory.createProperTestPatientRecord();

    @Test
    public void canCombineEvaluations() {
        assertEquals(Evaluation.PASS, combinedWithPass(TestEvaluationFunctionFactory.pass()));
        assertEquals(Evaluation.PASS, combinedWithPass(TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(Evaluation.PASS, combinedWithPass(TestEvaluationFunctionFactory.notEvaluated()));
        assertEquals(Evaluation.PASS, combinedWithPass(TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(Evaluation.PASS, combinedWithPass(TestEvaluationFunctionFactory.undetermined()));
        assertEquals(Evaluation.PASS, combinedWithPass(TestEvaluationFunctionFactory.fail()));

        assertEquals(Evaluation.PASS, combinedWithPassButWarn(TestEvaluationFunctionFactory.pass()));
        assertEquals(Evaluation.PASS_BUT_WARN, combinedWithPassButWarn(TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(Evaluation.PASS_BUT_WARN, combinedWithPassButWarn(TestEvaluationFunctionFactory.notEvaluated()));
        assertEquals(Evaluation.PASS_BUT_WARN, combinedWithPassButWarn(TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(Evaluation.PASS_BUT_WARN, combinedWithPassButWarn(TestEvaluationFunctionFactory.undetermined()));
        assertEquals(Evaluation.PASS_BUT_WARN, combinedWithPassButWarn(TestEvaluationFunctionFactory.fail()));

        assertEquals(Evaluation.PASS, combinedWithNotEvaluated(TestEvaluationFunctionFactory.pass()));
        assertEquals(Evaluation.PASS_BUT_WARN, combinedWithNotEvaluated(TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(Evaluation.NOT_EVALUATED, combinedWithNotEvaluated(TestEvaluationFunctionFactory.notEvaluated()));
        assertEquals(Evaluation.NOT_EVALUATED, combinedWithNotEvaluated(TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(Evaluation.NOT_EVALUATED, combinedWithNotEvaluated(TestEvaluationFunctionFactory.undetermined()));
        assertEquals(Evaluation.NOT_EVALUATED, combinedWithNotEvaluated(TestEvaluationFunctionFactory.fail()));

        assertEquals(Evaluation.PASS, combinedWithNotImplemented(TestEvaluationFunctionFactory.pass()));
        assertEquals(Evaluation.PASS_BUT_WARN, combinedWithNotImplemented(TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(Evaluation.NOT_EVALUATED, combinedWithNotImplemented(TestEvaluationFunctionFactory.notEvaluated()));
        assertEquals(Evaluation.NOT_IMPLEMENTED, combinedWithNotImplemented(TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(Evaluation.NOT_IMPLEMENTED, combinedWithNotImplemented(TestEvaluationFunctionFactory.undetermined()));
        assertEquals(Evaluation.NOT_IMPLEMENTED, combinedWithNotImplemented(TestEvaluationFunctionFactory.fail()));

        assertEquals(Evaluation.PASS, combinedWithUndetermined(TestEvaluationFunctionFactory.pass()));
        assertEquals(Evaluation.PASS_BUT_WARN, combinedWithUndetermined(TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(Evaluation.NOT_EVALUATED, combinedWithUndetermined(TestEvaluationFunctionFactory.notEvaluated()));
        assertEquals(Evaluation.NOT_IMPLEMENTED, combinedWithUndetermined(TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(Evaluation.UNDETERMINED, combinedWithUndetermined(TestEvaluationFunctionFactory.undetermined()));
        assertEquals(Evaluation.UNDETERMINED, combinedWithUndetermined(TestEvaluationFunctionFactory.fail()));

        assertEquals(Evaluation.PASS, combinedWithFail(TestEvaluationFunctionFactory.pass()));
        assertEquals(Evaluation.PASS_BUT_WARN, combinedWithFail(TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(Evaluation.NOT_EVALUATED, combinedWithFail(TestEvaluationFunctionFactory.notEvaluated()));
        assertEquals(Evaluation.NOT_IMPLEMENTED, combinedWithFail(TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(Evaluation.UNDETERMINED, combinedWithFail(TestEvaluationFunctionFactory.undetermined()));
        assertEquals(Evaluation.FAIL, combinedWithFail(TestEvaluationFunctionFactory.fail()));
    }

    @Test(expected = IllegalStateException.class)
    public void crashOnNoFunctionsToEvaluate() {
        new Or(Lists.newArrayList()).evaluate(TEST_PATIENT);
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
    private static Evaluation combinedWithNotEvaluated(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.notEvaluated(), function);
    }

    @NotNull
    private static Evaluation combinedWithNotImplemented(@NotNull EvaluationFunction function) {
        return evaluate(TestEvaluationFunctionFactory.notImplemented(), function);
    }

    @NotNull
    private static Evaluation evaluate(@NotNull EvaluationFunction function1, @NotNull EvaluationFunction function2) {
        return new Or(Lists.newArrayList(function1, function2)).evaluate(TEST_PATIENT);
    }
}