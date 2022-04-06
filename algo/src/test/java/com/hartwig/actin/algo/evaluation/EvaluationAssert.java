package com.hartwig.actin.algo.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.jetbrains.annotations.NotNull;

public final class EvaluationAssert {

    private EvaluationAssert() {
    }

    public static void assertEvaluation(@NotNull EvaluationResult expected, @NotNull Evaluation actual) {
        assertEquals(expected, actual.result());

        if (actual.result() == EvaluationResult.PASS) {
            assertFalse(actual.passSpecificMessages().isEmpty());
            assertTrue(actual.warnSpecificMessages().isEmpty());
            assertTrue(actual.warnGeneralMessages().isEmpty());
            assertTrue(actual.undeterminedSpecificMessages().isEmpty());
            assertTrue(actual.undeterminedGeneralMessages().isEmpty());
            assertTrue(actual.failSpecificMessages().isEmpty());
            assertTrue(actual.failGeneralMessages().isEmpty());
        } else if (actual.result() == EvaluationResult.WARN) {
            assertTrue(actual.passSpecificMessages().isEmpty());
            assertTrue(actual.passGeneralMessages().isEmpty());
            assertFalse(actual.warnSpecificMessages().isEmpty());
            assertTrue(actual.undeterminedSpecificMessages().isEmpty());
            assertTrue(actual.undeterminedGeneralMessages().isEmpty());
            assertTrue(actual.failSpecificMessages().isEmpty());
            assertTrue(actual.failGeneralMessages().isEmpty());
        } else if (actual.result() == EvaluationResult.UNDETERMINED) {
            assertTrue(actual.passSpecificMessages().isEmpty());
            assertTrue(actual.passSpecificMessages().isEmpty());
            assertTrue(actual.passGeneralMessages().isEmpty());
            assertTrue(actual.warnSpecificMessages().isEmpty());
            assertTrue(actual.warnGeneralMessages().isEmpty());
            assertFalse(actual.undeterminedSpecificMessages().isEmpty());
            assertTrue(actual.failSpecificMessages().isEmpty());
            assertTrue(actual.failGeneralMessages().isEmpty());
        } else if (actual.result() == EvaluationResult.FAIL) {
            assertTrue(actual.passSpecificMessages().isEmpty());
            assertTrue(actual.passGeneralMessages().isEmpty());
            assertTrue(actual.warnSpecificMessages().isEmpty());
            assertTrue(actual.warnGeneralMessages().isEmpty());
            assertTrue(actual.undeterminedSpecificMessages().isEmpty());
            assertTrue(actual.undeterminedGeneralMessages().isEmpty());
            assertFalse(actual.failSpecificMessages().isEmpty());
        }
    }
}
