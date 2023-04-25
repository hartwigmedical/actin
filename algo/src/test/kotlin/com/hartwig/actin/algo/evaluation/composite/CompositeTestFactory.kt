package com.hartwig.actin.algo.evaluation.composite;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;

import org.jetbrains.annotations.NotNull;

final class CompositeTestFactory {

    private static final EvaluationResult DEFAULT_RESULT = EvaluationResult.PASS;

    private static final boolean DEFAULT_RECOVERABLE = false;
    private static final boolean DEFAULT_INCLUDE_MOLECULAR = false;
    private static final int DEFAULT_INDEX = 1;

    private CompositeTestFactory() {
    }

    @NotNull
    public static Evaluation create(@NotNull EvaluationResult result, boolean includeMolecular) {
        return create(result, DEFAULT_RECOVERABLE, includeMolecular, DEFAULT_INDEX);
    }

    @NotNull
    public static Evaluation create(boolean recoverable, int index) {
        return create(DEFAULT_RESULT, recoverable, DEFAULT_INCLUDE_MOLECULAR, index);
    }

    @NotNull
    public static Evaluation create(@NotNull EvaluationResult result, int index) {
        return create(result, DEFAULT_RECOVERABLE, DEFAULT_INCLUDE_MOLECULAR, index);
    }

    @NotNull
    public static Evaluation create(@NotNull EvaluationResult result, boolean includeMolecular, int index) {
        return create(result, DEFAULT_RECOVERABLE, includeMolecular, index);
    }

    @NotNull
    private static Evaluation create(@NotNull EvaluationResult result, boolean recoverable, boolean includeMolecular, int index) {
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder()
                .result(result)
                .recoverable(recoverable)
                .addPassSpecificMessages("pass specific " + index)
                .addPassGeneralMessages("pass general " + index)
                .addWarnSpecificMessages("warn specific " + index)
                .addWarnGeneralMessages("warn general " + index)
                .addUndeterminedSpecificMessages("undetermined specific " + index)
                .addUndeterminedGeneralMessages("undetermined general " + index)
                .addFailSpecificMessages("fail specific " + index)
                .addFailGeneralMessages("fail general " + index);

        if (includeMolecular) {
            builder.addInclusionMolecularEvents("inclusion event " + index);
            builder.addExclusionMolecularEvents("exclusion event " + index);
        }

        return builder.build();
    }
}
