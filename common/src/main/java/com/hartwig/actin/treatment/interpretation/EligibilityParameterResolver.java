package com.hartwig.actin.treatment.interpretation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class EligibilityParameterResolver {

    private static final Logger LOGGER = LogManager.getLogger(EligibilityParameterResolver.class);

    private EligibilityParameterResolver() {
    }

    public static boolean hasValidParameters(@NotNull EligibilityFunction function) {
        try {
            switch (function.rule()) {
                case AND: {
                    createCompositeParameters(function.parameters(), 2);
                    return true;
                }
                case IS_AT_LEAST_18_YEARS_OLD: {
                    return function.parameters().isEmpty();
                }
                default: {
                    LOGGER.warn("Could not determine validity of parameters for function with rule '{}'", function.rule());
                    return false;
                }
            }
        } catch (Exception exception) {
            return false;
        }
    }

    @NotNull
    public static List<EligibilityFunction> createCompositeParameters(@NotNull List<Object> inputs, int expectedCount) {
        if (inputs.size() != expectedCount) {
            throw new IllegalArgumentException("Invalid number of inputs passed to composite function: " + inputs.size());
        }

        List<EligibilityFunction> functions = Lists.newArrayList();
        for (Object input : inputs) {
            functions.add((EligibilityFunction) input);
        }
        return functions;
    }
}
