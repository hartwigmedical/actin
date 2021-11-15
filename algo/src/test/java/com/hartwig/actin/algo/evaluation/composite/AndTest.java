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
        assertEquals(Evaluation.PASS, evaluate(TestEvaluationFunctionFactory.pass(), TestEvaluationFunctionFactory.pass()));
        assertEquals(Evaluation.PASS_BUT_WARN, evaluate(TestEvaluationFunctionFactory.pass(), TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(Evaluation.NOT_IMPLEMENTED,
                evaluate(TestEvaluationFunctionFactory.pass(), TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(Evaluation.UNDETERMINED, evaluate(TestEvaluationFunctionFactory.pass(), TestEvaluationFunctionFactory.undetermined()));
        assertEquals(Evaluation.FAIL, evaluate(TestEvaluationFunctionFactory.pass(), TestEvaluationFunctionFactory.fail()));

        assertEquals(Evaluation.PASS_BUT_WARN, evaluate(TestEvaluationFunctionFactory.passButWarn(), TestEvaluationFunctionFactory.pass()));
        assertEquals(Evaluation.PASS_BUT_WARN,
                evaluate(TestEvaluationFunctionFactory.passButWarn(), TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(Evaluation.NOT_IMPLEMENTED,
                evaluate(TestEvaluationFunctionFactory.passButWarn(), TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(Evaluation.UNDETERMINED,
                evaluate(TestEvaluationFunctionFactory.passButWarn(), TestEvaluationFunctionFactory.undetermined()));
        assertEquals(Evaluation.FAIL, evaluate(TestEvaluationFunctionFactory.passButWarn(), TestEvaluationFunctionFactory.fail()));

        assertEquals(Evaluation.NOT_IMPLEMENTED,
                evaluate(TestEvaluationFunctionFactory.notImplemented(), TestEvaluationFunctionFactory.pass()));
        assertEquals(Evaluation.NOT_IMPLEMENTED,
                evaluate(TestEvaluationFunctionFactory.notImplemented(), TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(Evaluation.NOT_IMPLEMENTED,
                evaluate(TestEvaluationFunctionFactory.notImplemented(), TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(Evaluation.UNDETERMINED,
                evaluate(TestEvaluationFunctionFactory.notImplemented(), TestEvaluationFunctionFactory.undetermined()));
        assertEquals(Evaluation.FAIL, evaluate(TestEvaluationFunctionFactory.notImplemented(), TestEvaluationFunctionFactory.fail()));

        assertEquals(Evaluation.UNDETERMINED, evaluate(TestEvaluationFunctionFactory.undetermined(), TestEvaluationFunctionFactory.pass()));
        assertEquals(Evaluation.UNDETERMINED,
                evaluate(TestEvaluationFunctionFactory.undetermined(), TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(Evaluation.UNDETERMINED,
                evaluate(TestEvaluationFunctionFactory.undetermined(), TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(Evaluation.UNDETERMINED,
                evaluate(TestEvaluationFunctionFactory.undetermined(), TestEvaluationFunctionFactory.undetermined()));
        assertEquals(Evaluation.FAIL, evaluate(TestEvaluationFunctionFactory.undetermined(), TestEvaluationFunctionFactory.fail()));

        assertEquals(Evaluation.FAIL, evaluate(TestEvaluationFunctionFactory.fail(), TestEvaluationFunctionFactory.pass()));
        assertEquals(Evaluation.FAIL, evaluate(TestEvaluationFunctionFactory.fail(), TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(Evaluation.FAIL, evaluate(TestEvaluationFunctionFactory.fail(), TestEvaluationFunctionFactory.notImplemented()));
        assertEquals(Evaluation.FAIL, evaluate(TestEvaluationFunctionFactory.fail(), TestEvaluationFunctionFactory.undetermined()));
        assertEquals(Evaluation.FAIL, evaluate(TestEvaluationFunctionFactory.fail(), TestEvaluationFunctionFactory.fail()));
    }

    @Test(expected = IllegalStateException.class)
    public void crashOnNoFunctionsToEvaluate() {
        new And(Lists.newArrayList()).evaluate(TEST_PATIENT);
    }

    @NotNull
    private static Evaluation evaluate(@NotNull EvaluationFunction function1, @NotNull EvaluationFunction function2) {
        return new And(Lists.newArrayList(function1, function2)).evaluate(TEST_PATIENT);
    }
}