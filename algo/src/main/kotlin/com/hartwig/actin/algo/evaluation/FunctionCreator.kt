package com.hartwig.actin.algo.evaluation;

import com.hartwig.actin.treatment.datamodel.EligibilityFunction;

import org.jetbrains.annotations.NotNull;

public interface FunctionCreator {

    @NotNull
    EvaluationFunction create(@NotNull EligibilityFunction function);
}
