package com.hartwig.actin.algo.evaluation.othercondition;

import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public final class OtherConditionFunctionFactory {

    private OtherConditionFunctionFactory() {
    }

    @NotNull
    public static EvaluationFunction createPriorConditionWithDoidFunction(@NotNull DoidModel doidModel, @NotNull String doidToFind) {
        return new HasHadPriorConditionWithDoid(doidModel, doidToFind);
    }
}
