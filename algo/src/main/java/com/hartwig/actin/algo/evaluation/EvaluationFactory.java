package com.hartwig.actin.algo.evaluation;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
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

    public static Evaluation pass(String specificMessage, String generalMessage) {
        return unrecoverable().result(EvaluationResult.PASS)
                .addPassSpecificMessages(specificMessage)
                .addPassGeneralMessages(generalMessage)
                .build();
    }

    public static Evaluation fail(String specificMessage, String generalMessage) {
        return buildFailEvaluation(unrecoverable(), specificMessage, generalMessage);
    }

    public static Evaluation recoverableFail(String specificMessage, String generalMessage) {
        return buildFailEvaluation(recoverable(), specificMessage, generalMessage);
    }

    public static Evaluation undetermined(String specificMessage, String generalMessage) {
        return unrecoverable().result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages(specificMessage)
                .addUndeterminedGeneralMessages(generalMessage)
                .build();
    }

    public static Evaluation warn(String specificMessage, String generalMessage) {
        return unrecoverable().result(EvaluationResult.WARN)
                .addWarnSpecificMessages(specificMessage)
                .addWarnGeneralMessages(generalMessage)
                .build();
    }

    private static Evaluation buildFailEvaluation(ImmutableEvaluation.Builder builder, String specificMessage, String generalMessage) {
        return builder.result(EvaluationResult.FAIL)
                .addFailSpecificMessages(specificMessage)
                .addFailGeneralMessages(generalMessage)
                .build();
    }
}
