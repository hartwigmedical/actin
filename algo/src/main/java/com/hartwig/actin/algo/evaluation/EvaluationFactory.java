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
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.PASS)
                .addPassSpecificMessages(specificMessage)
                .addPassGeneralMessages(generalMessage)
                .build();
    }

    public static Evaluation fail(String specificMessage, String generalMessage) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages(specificMessage)
                .addFailGeneralMessages(generalMessage)
                .build();
    }

    public static Evaluation undetermined(String specificMessage, String generalMessage) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages(specificMessage)
                .addUndeterminedGeneralMessages(generalMessage)
                .build();
    }
}
