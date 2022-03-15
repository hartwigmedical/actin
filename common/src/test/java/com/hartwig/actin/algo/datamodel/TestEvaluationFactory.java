package com.hartwig.actin.algo.datamodel;

import org.jetbrains.annotations.NotNull;

public final class TestEvaluationFactory {

    private TestEvaluationFactory() {
    }

    @NotNull
    public static Evaluation withResult(@NotNull EvaluationResult result) {
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);

        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("fail");
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedSpecificMessages("undetermined");
        } else if (result.isPass()) {
            builder.addPassSpecificMessages("pass");
        }

        return builder.build();
    }
}
