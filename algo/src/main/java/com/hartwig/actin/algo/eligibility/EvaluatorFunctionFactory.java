package com.hartwig.actin.algo.eligibility;

import java.time.LocalDate;

import com.hartwig.actin.treatment.datamodel.EligibilityFunction;

import org.jetbrains.annotations.NotNull;

public final class EvaluatorFunctionFactory {

    private EvaluatorFunctionFactory() {
    }

    @NotNull
    public static EvaluatorFunction create(@NotNull EligibilityFunction eligibilityFunction) {
        switch (eligibilityFunction.rule()) {
            case IS_AT_LEAST_18_YEARS_OLD:
                return createIsAdult();
            default:
                throw new IllegalArgumentException("Cannot create evaluator function for rule " + eligibilityFunction.rule());
        }
    }

    @NotNull
    private static EvaluatorFunction createIsAdult() {
        return new IsAdult(LocalDate.now().getYear());
    }
}
