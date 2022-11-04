package com.hartwig.actin.algo.evaluation.composite;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

public class AndTest {

    private static final PatientRecord TEST_PATIENT = TestDataFactory.createProperTestPatientRecord();

    @Test
    public void canCombineEvaluations() {
        assertEvaluation(EvaluationResult.NOT_EVALUATED, combineWithNotEvaluated(TestEvaluationFunctionFactory.notEvaluated()));
        assertEvaluation(EvaluationResult.PASS, combineWithNotEvaluated(TestEvaluationFunctionFactory.pass()));
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithNotEvaluated(TestEvaluationFunctionFactory.undetermined()));
        assertEvaluation(EvaluationResult.WARN, combineWithNotEvaluated(TestEvaluationFunctionFactory.warn()));
        assertEvaluation(EvaluationResult.FAIL, combineWithNotEvaluated(TestEvaluationFunctionFactory.fail()));
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithNotEvaluated(TestEvaluationFunctionFactory.notImplemented()));

        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.notEvaluated()));
        assertEvaluation(EvaluationResult.PASS, combineWithPass(TestEvaluationFunctionFactory.pass()));
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithPass(TestEvaluationFunctionFactory.undetermined()));
        assertEvaluation(EvaluationResult.WARN, combineWithPass(TestEvaluationFunctionFactory.warn()));
        assertEvaluation(EvaluationResult.FAIL, combineWithPass(TestEvaluationFunctionFactory.fail()));
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithPass(TestEvaluationFunctionFactory.notImplemented()));

        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.notEvaluated()));
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.pass()));
        assertEvaluation(EvaluationResult.UNDETERMINED, combineWithUndetermined(TestEvaluationFunctionFactory.undetermined()));
        assertEvaluation(EvaluationResult.WARN, combineWithUndetermined(TestEvaluationFunctionFactory.warn()));
        assertEvaluation(EvaluationResult.FAIL, combineWithUndetermined(TestEvaluationFunctionFactory.fail()));
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithUndetermined(TestEvaluationFunctionFactory.notImplemented()));

        assertEvaluation(EvaluationResult.WARN, combineWithWarn(TestEvaluationFunctionFactory.notEvaluated()));
        assertEvaluation(EvaluationResult.WARN, combineWithWarn(TestEvaluationFunctionFactory.pass()));
        assertEvaluation(EvaluationResult.WARN, combineWithWarn(TestEvaluationFunctionFactory.undetermined()));
        assertEvaluation(EvaluationResult.WARN, combineWithWarn(TestEvaluationFunctionFactory.warn()));
        assertEvaluation(EvaluationResult.FAIL, combineWithWarn(TestEvaluationFunctionFactory.fail()));
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithWarn(TestEvaluationFunctionFactory.notImplemented()));

        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.notEvaluated()));
        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.pass()));
        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.undetermined()));
        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.warn()));
        assertEvaluation(EvaluationResult.FAIL, combineWithFail(TestEvaluationFunctionFactory.fail()));
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithFail(TestEvaluationFunctionFactory.notImplemented()));

        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithNotImplemented(TestEvaluationFunctionFactory.notEvaluated()));
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithNotImplemented(TestEvaluationFunctionFactory.pass()));
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithNotImplemented(TestEvaluationFunctionFactory.undetermined()));
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithNotImplemented(TestEvaluationFunctionFactory.warn()));
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithNotImplemented(TestEvaluationFunctionFactory.fail()));
        assertEvaluation(EvaluationResult.NOT_IMPLEMENTED, combineWithNotImplemented(TestEvaluationFunctionFactory.notImplemented()));
    }

    @Test
    public void canRetainMessages() {
        EvaluationFunction function1 = record -> CompositeTestFactory.create(EvaluationResult.FAIL, 1);
        EvaluationFunction function2 = record -> CompositeTestFactory.create(EvaluationResult.FAIL, 2);
        EvaluationFunction function3 = record -> CompositeTestFactory.create(EvaluationResult.PASS, 3);
        EvaluationFunction function4 = record -> CompositeTestFactory.create(EvaluationResult.PASS, 4);

        Evaluation result = new And(Lists.newArrayList(function1, function2, function3, function4)).evaluate(TEST_PATIENT);

        assertEquals(2, result.passSpecificMessages().size());
        assertTrue(result.passSpecificMessages().contains("pass specific 1"));
        assertTrue(result.passSpecificMessages().contains("pass specific 2"));

        assertEquals(2, result.passGeneralMessages().size());
        assertTrue(result.passGeneralMessages().contains("pass general 1"));
        assertTrue(result.passGeneralMessages().contains("pass general 2"));

        assertEquals(2, result.warnSpecificMessages().size());
        assertTrue(result.warnSpecificMessages().contains("warn specific 1"));
        assertTrue(result.warnSpecificMessages().contains("warn specific 2"));

        assertEquals(2, result.warnGeneralMessages().size());
        assertTrue(result.warnGeneralMessages().contains("warn general 1"));
        assertTrue(result.warnGeneralMessages().contains("warn general 2"));

        assertEquals(2, result.failSpecificMessages().size());
        assertTrue(result.failSpecificMessages().contains("fail specific 1"));
        assertTrue(result.failSpecificMessages().contains("fail specific 2"));

        assertEquals(2, result.failGeneralMessages().size());
        assertTrue(result.failGeneralMessages().contains("fail general 1"));
        assertTrue(result.failGeneralMessages().contains("fail general 2"));

        assertEquals(2, result.undeterminedSpecificMessages().size());
        assertTrue(result.undeterminedSpecificMessages().contains("undetermined specific 1"));
        assertTrue(result.undeterminedSpecificMessages().contains("undetermined specific 2"));

        assertEquals(2, result.undeterminedGeneralMessages().size());
        assertTrue(result.undeterminedGeneralMessages().contains("undetermined general 1"));
        assertTrue(result.undeterminedGeneralMessages().contains("undetermined general 2"));
    }

    @Test
    public void combinesMolecularInclusionExclusionEvents() {
        EvaluationFunction function1 = record -> CompositeTestFactory.create(EvaluationResult.FAIL, true, 1);
        EvaluationFunction function2 = record -> CompositeTestFactory.create(EvaluationResult.FAIL, true, 2);
        EvaluationFunction function3 = record -> CompositeTestFactory.create(EvaluationResult.PASS, true, 3);

        Evaluation result = new And(Lists.newArrayList(function1, function2, function3)).evaluate(TEST_PATIENT);

        assertEquals(2, result.inclusionMolecularEvents().size());
        assertTrue(result.inclusionMolecularEvents().contains("inclusion event 1"));
        assertTrue(result.inclusionMolecularEvents().contains("inclusion event 2"));

        assertEquals(2, result.exclusionMolecularEvents().size());
        assertTrue(result.exclusionMolecularEvents().contains("exclusion event 1"));
        assertTrue(result.exclusionMolecularEvents().contains("exclusion event 2"));
    }

    @Test
    public void properlyRespectsRecoverable() {
        EvaluationFunction recoverable = record -> CompositeTestFactory.create(true, 1);
        EvaluationFunction unrecoverable = record -> CompositeTestFactory.create(false, 2);

        Evaluation result = new And(Lists.newArrayList(recoverable, unrecoverable)).evaluate(TEST_PATIENT);

        assertFalse(result.recoverable());
        assertEquals(1, result.undeterminedGeneralMessages().size());
        assertTrue(result.undeterminedGeneralMessages().contains("undetermined general 2"));
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
        return new And(Lists.newArrayList(function1, function2)).evaluate(TEST_PATIENT);
    }
}