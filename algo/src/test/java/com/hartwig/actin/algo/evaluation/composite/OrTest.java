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

    private static final PatientRecord TEST_PATIENT = TestDataFactory.createTestPatientRecord();

    @Test
    public void canCombineEvaluations() {
        assertEquals(Evaluation.PASS, evaluate(TestEvaluationFunctionFactory.pass(), TestEvaluationFunctionFactory.pass()));
        assertEquals(Evaluation.PASS, evaluate(TestEvaluationFunctionFactory.pass(), TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(Evaluation.PASS, evaluate(TestEvaluationFunctionFactory.pass(), TestEvaluationFunctionFactory.fail()));
        assertEquals(Evaluation.PASS, evaluate(TestEvaluationFunctionFactory.pass(), TestEvaluationFunctionFactory.couldNotBeDetermined()));

        assertEquals(Evaluation.PASS, evaluate(TestEvaluationFunctionFactory.passButWarn(), TestEvaluationFunctionFactory.pass()));
        assertEquals(Evaluation.PASS_BUT_WARN,
                evaluate(TestEvaluationFunctionFactory.passButWarn(), TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(Evaluation.PASS_BUT_WARN, evaluate(TestEvaluationFunctionFactory.passButWarn(), TestEvaluationFunctionFactory.fail()));
        assertEquals(Evaluation.PASS_BUT_WARN,
                evaluate(TestEvaluationFunctionFactory.passButWarn(), TestEvaluationFunctionFactory.couldNotBeDetermined()));

        assertEquals(Evaluation.PASS, evaluate(TestEvaluationFunctionFactory.fail(), TestEvaluationFunctionFactory.pass()));
        assertEquals(Evaluation.PASS_BUT_WARN, evaluate(TestEvaluationFunctionFactory.fail(), TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(Evaluation.FAIL, evaluate(TestEvaluationFunctionFactory.fail(), TestEvaluationFunctionFactory.fail()));
        assertEquals(Evaluation.FAIL, evaluate(TestEvaluationFunctionFactory.fail(), TestEvaluationFunctionFactory.couldNotBeDetermined()));

        assertEquals(Evaluation.PASS, evaluate(TestEvaluationFunctionFactory.couldNotBeDetermined(), TestEvaluationFunctionFactory.pass()));
        assertEquals(Evaluation.PASS_BUT_WARN,
                evaluate(TestEvaluationFunctionFactory.couldNotBeDetermined(), TestEvaluationFunctionFactory.passButWarn()));
        assertEquals(Evaluation.FAIL, evaluate(TestEvaluationFunctionFactory.couldNotBeDetermined(), TestEvaluationFunctionFactory.fail()));
        assertEquals(Evaluation.COULD_NOT_BE_DETERMINED,
                evaluate(TestEvaluationFunctionFactory.couldNotBeDetermined(), TestEvaluationFunctionFactory.couldNotBeDetermined()));
    }

    @Test(expected = IllegalStateException.class)
    public void crashOnNoFunctionsToEvaluate() {
        new Or(Lists.newArrayList()).evaluate(TEST_PATIENT);
    }

    @NotNull
    private static Evaluation evaluate(@NotNull EvaluationFunction function1, @NotNull EvaluationFunction function2) {
        return new Or(Lists.newArrayList(function1, function2)).evaluate(TEST_PATIENT);
    }
}