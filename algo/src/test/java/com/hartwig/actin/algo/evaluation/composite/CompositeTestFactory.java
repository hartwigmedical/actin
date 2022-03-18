package com.hartwig.actin.algo.evaluation.composite;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;

import org.jetbrains.annotations.NotNull;

final class CompositeTestFactory {

    private CompositeTestFactory() {
    }

    @NotNull
    public static Evaluation create(@NotNull EvaluationResult result) {
        return create(result, 1);
    }

    @NotNull
    public static Evaluation create(@NotNull EvaluationResult result, int index) {
        return ImmutableEvaluation.builder()
                .result(result)
                .recoverable(false)
                .addPassSpecificMessages("pass specific " + index)
                .addPassGeneralMessages("pass general " + index)
                .addWarnSpecificMessages("warn specific " + index)
                .addWarnGeneralMessages("warn general " + index)
                .addUndeterminedSpecificMessages("undetermined specific " + index)
                .addUndeterminedGeneralMessages("undetermined general " + index)
                .addFailSpecificMessages("fail specific " + index)
                .addFailGeneralMessages("fail general " + index)
                .build();
    }
}
