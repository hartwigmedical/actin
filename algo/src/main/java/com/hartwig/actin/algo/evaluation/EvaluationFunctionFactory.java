package com.hartwig.actin.algo.evaluation;

import java.time.LocalDate;

import com.hartwig.actin.treatment.datamodel.EligibilityFunction;

import org.jetbrains.annotations.NotNull;

public final class EvaluationFunctionFactory {

    private EvaluationFunctionFactory() {
    }

    @NotNull
    public static EvaluationFunction create(@NotNull EligibilityFunction eligibilityFunction) {
        switch (eligibilityFunction.rule()) {
            case IS_AT_LEAST_18_YEARS_OLD:
                return createIsAdult();
            default:
                throw new IllegalArgumentException("Cannot create evaluator function for rule " + eligibilityFunction.rule());
        }
    }

    @NotNull
    private static EvaluationFunction createIsAdult() {
        return new IsAtLeastEighteenYearsOld(LocalDate.now().getYear());
    }
}
