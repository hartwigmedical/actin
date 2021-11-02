package com.hartwig.actin.algo.evaluation;

import java.time.LocalDate;
import java.util.List;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.composite.And;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.interpretation.EligibilityParameterResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class EvaluationFunctionFactory {

    private static final Logger LOGGER = LogManager.getLogger(EvaluationFunctionFactory.class);

    private EvaluationFunctionFactory() {
    }

    @NotNull
    public static EvaluationFunction create(@NotNull EligibilityFunction function) {
        if (!EligibilityParameterResolver.hasValidParameters(function)) {
            LOGGER.warn("Could not create function with rule '{}' based on inputs {}", function.rule(), function.parameters());
            return createAlwaysFail();
        }

        switch (function.rule()) {
            case AND:
                return createAnd(function.parameters());
            case IS_AT_LEAST_18_YEARS_OLD:
                return createIsAtLeast18YearsOld();
            default: {
                LOGGER.warn("No evaluation function implemented for '{}'. Evaluation for this rule will always fail", function.rule());
                return createAlwaysFail();
            }
        }
    }

    @NotNull
    private static EvaluationFunction createAnd(@NotNull List<Object> parameters) {
        List<EligibilityFunction> functions = EligibilityParameterResolver.createCompositeParameters(parameters, 2);
        return new And(create(functions.get(0)), create(functions.get(1)));
    }

    @NotNull
    private static EvaluationFunction createIsAtLeast18YearsOld() {
        return new IsAtLeastEighteenYearsOld(LocalDate.now().getYear());
    }

    @NotNull
    private static EvaluationFunction createAlwaysFail() {
        return record -> Evaluation.FAIL;
    }
}
