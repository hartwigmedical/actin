package com.hartwig.actin.algo.datamodel;

import org.jetbrains.annotations.NotNull;

public final class TestEvaluationFactory {

    private TestEvaluationFactory() {
    }

    @NotNull
    public static Evaluation withResult(@NotNull EvaluationResult result) {
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);

        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("fail");
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedMessages("undetermined");
        } else if (result.isPass()) {
            builder.addPassMessages("pass");
        }

        return builder.build();
    }
}
