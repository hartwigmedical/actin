package com.hartwig.actin.algo.evaluation;

import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;

import org.jetbrains.annotations.NotNull;

public final class EvaluationFactory {

    private EvaluationFactory() {
    }

    @NotNull
    public static ImmutableEvaluation.Builder recoverable() {
        return ImmutableEvaluation.builder().recoverable(true);
    }

    @NotNull
    public static ImmutableEvaluation.Builder unrecoverable() {
        return ImmutableEvaluation.builder().recoverable(false);
    }
}
