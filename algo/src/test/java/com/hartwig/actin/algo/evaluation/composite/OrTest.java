package com.hartwig.actin.algo.evaluation.composite;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
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
        EvaluationFunction function1 = record -> CompositeTestFactory.create(EvaluationResult.FAIL, 1);
        EvaluationFunction function2 = record -> CompositeTestFactory.create(EvaluationResult.FAIL, 2);
        EvaluationFunction function3 = record -> CompositeTestFactory.create(EvaluationResult.PASS, 3);
        EvaluationFunction function4 = record -> CompositeTestFactory.create(EvaluationResult.PASS, 4);

        Evaluation result = new Or(Lists.newArrayList(function1, function2, function3, function4)).evaluate(TEST_PATIENT);

        assertEquals(2, result.passSpecificMessages().size());
        assertTrue(result.passSpecificMessages().contains("pass specific 3"));
        assertTrue(result.passSpecificMessages().contains("pass specific 4"));

        assertEquals(2, result.passGeneralMessages().size());
        assertTrue(result.passGeneralMessages().contains("pass general 3"));
        assertTrue(result.passGeneralMessages().contains("pass general 4"));

        assertEquals(2, result.warnSpecificMessages().size());
        assertTrue(result.warnSpecificMessages().contains("warn specific 3"));
        assertTrue(result.warnSpecificMessages().contains("warn specific 4"));

        assertEquals(2, result.warnGeneralMessages().size());
        assertTrue(result.warnGeneralMessages().contains("warn general 3"));
        assertTrue(result.warnGeneralMessages().contains("warn general 4"));

        assertEquals(2, result.failSpecificMessages().size());
        assertTrue(result.failSpecificMessages().contains("fail specific 3"));
        assertTrue(result.failSpecificMessages().contains("fail specific 4"));

        assertEquals(2, result.failGeneralMessages().size());
        assertTrue(result.failGeneralMessages().contains("fail general 3"));
        assertTrue(result.failGeneralMessages().contains("fail general 4"));

        assertEquals(2, result.undeterminedSpecificMessages().size());
        assertTrue(result.undeterminedSpecificMessages().contains("undetermined specific 3"));
        assertTrue(result.undeterminedSpecificMessages().contains("undetermined specific 4"));

        assertEquals(2, result.undeterminedGeneralMessages().size());
        assertTrue(result.undeterminedGeneralMessages().contains("undetermined general 3"));
        assertTrue(result.undeterminedGeneralMessages().contains("undetermined general 4"));
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