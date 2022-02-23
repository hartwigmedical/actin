package com.hartwig.actin.algo.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.jetbrains.annotations.NotNull;

public final class EvaluationAssert {

    private EvaluationAssert() {
    }

    public static void assertEvaluation(@NotNull EvaluationResult expected, @NotNull Evaluation actual) {
        assertEquals(expected, actual.result());
        // TODO Check whether every rule adds a message of its expected type.
        if (actual.result() == EvaluationResult.FAIL) {
            assertTrue(actual.passMessages().isEmpty());
            assertTrue(actual.undeterminedMessages().isEmpty());
        } else if (actual.result() == EvaluationResult.UNDETERMINED) {
            assertTrue(actual.passMessages().isEmpty());
            assertTrue(actual.failMessages().isEmpty());
        } else if (actual.result().isPass()) {
            assertTrue(actual.undeterminedMessages().isEmpty());
            assertTrue(actual.failMessages().isEmpty());
        }
    }
}
