package com.hartwig.actin.algo.datamodel;

import org.jetbrains.annotations.NotNull;

public final class TestEvaluationFactory {

    private TestEvaluationFactory() {
    }

    @NotNull
    public static Evaluation withResult(@NotNull EvaluationResult result) {
        return ImmutableEvaluation.builder().result(result).build();
    }
}
