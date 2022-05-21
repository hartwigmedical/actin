package com.hartwig.actin.algo.datamodel;

import org.jetbrains.annotations.NotNull;

public final class EvaluationTestFactory {

    private EvaluationTestFactory() {
    }

    @NotNull
    public static Evaluation withResult(@NotNull EvaluationResult result) {
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().recoverable(false).result(result);

        if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("pass specific");
            builder.addPassGeneralMessages("pass general");
        } else if (result == EvaluationResult.NOT_EVALUATED) {
            builder.addPassSpecificMessages("not evaluated specific");
            builder.addPassGeneralMessages("not evaluated general");
        } else if (result == EvaluationResult.WARN) {
            builder.addWarnSpecificMessages("warn specific");
            builder.addWarnGeneralMessages("warn general");
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedSpecificMessages("undetermined specific");
            builder.addUndeterminedGeneralMessages("undetermined general");
        } else if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("fail specific");
            builder.addFailGeneralMessages("fail general");
        }

        return builder.build();
    }
}
