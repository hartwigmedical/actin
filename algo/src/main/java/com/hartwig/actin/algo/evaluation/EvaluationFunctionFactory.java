package com.hartwig.actin.algo.evaluation;

import java.time.LocalDate;

import com.hartwig.actin.algo.datamodel.EligibilityEvaluation;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class EvaluationFunctionFactory {

    private static final Logger LOGGER = LogManager.getLogger(EvaluationFunctionFactory.class);

    private EvaluationFunctionFactory() {
    }

    @NotNull
    public static EvaluationFunction create(@NotNull EligibilityFunction eligibilityFunction) {
        switch (eligibilityFunction.rule()) {
            case IS_AT_LEAST_18_YEARS_OLD:
                return createIsAdult();
            default: {
                LOGGER.warn("No evaluation function implemented for '{}'. Will always fail", eligibilityFunction.rule());
                return createAlwaysFail();
            }
        }
    }

    @NotNull
    private static EvaluationFunction createIsAdult() {
        return new IsAtLeastEighteenYearsOld(LocalDate.now().getYear());
    }

    @NotNull
    private static EvaluationFunction createAlwaysFail() {
        return record -> EligibilityEvaluation.FAIL;
    }
}
