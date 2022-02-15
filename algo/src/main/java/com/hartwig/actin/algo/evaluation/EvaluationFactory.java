package com.hartwig.actin.algo.evaluation;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;

import org.jetbrains.annotations.NotNull;

public final class EvaluationFactory {

    private EvaluationFactory() {
    }

    @NotNull
    public static Evaluation create(@NotNull EvaluationResult result) {
        return ImmutableEvaluation.builder().result(result).build();
    }
}
