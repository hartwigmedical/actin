package com.hartwig.actin.treatment.trial;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;
import com.hartwig.actin.treatment.interpretation.EligibilityParameterResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EligibilityFactory {

    private static final Logger LOGGER = LogManager.getLogger(EligibilityFactory.class);

    private static final char COMPOSITE_START = '(';
    private static final char COMPOSITE_END = ')';
    private static final char PARAM_START = '[';
    private static final char PARAM_END = ']';

    private EligibilityFactory() {
    }

    public static boolean isValidInclusionCriterion(@NotNull String criterion) {
        try {
            generateEligibilityFunction(criterion);
            return true;
        } catch (Exception exc) {
            LOGGER.debug(exc.getMessage());
            return false;
        }
    }

    @NotNull
    public static EligibilityFunction generateEligibilityFunction(@NotNull String criterion) {
        EligibilityRule rule;
        List<Object> parameters = Lists.newArrayList();
        if (isCompositeCriterion(criterion)) {
            rule = extractCompositeRule(criterion.trim());
            for (String compositeInput : extractCompositeInputs(criterion)) {
                parameters.add(generateEligibilityFunction(compositeInput));
            }
        } else if (isParameterizedCriterion(criterion)) {
            rule = extractParameterizedRule(criterion);
            parameters.addAll(extractParameterizedInputs(criterion));
        } else {
            rule = EligibilityRule.valueOf(criterion.trim());
        }

        EligibilityFunction function = ImmutableEligibilityFunction.builder().rule(rule).parameters(parameters).build();
        if (!EligibilityParameterResolver.hasValidParameters(function)) {
            throw new IllegalStateException("Function " + function.rule() + " has invalid parameters: '" + function.parameters() + "'");
        }
        return function;
    }

    @NotNull
    private static EligibilityRule extractCompositeRule(@NotNull String criterion) {
        EligibilityRule rule = EligibilityRule.valueOf(criterion.substring(0, criterion.indexOf(COMPOSITE_START)));
        if (!EligibilityParameterResolver.COMPOSITE_RULES.contains(rule)) {
            throw new IllegalStateException("Not a valid composite rule: '" + rule + "'");
        }
        return rule;
    }

    @NotNull
    private static EligibilityRule extractParameterizedRule(@NotNull String criterion) {
        return EligibilityRule.valueOf(criterion.substring(0, criterion.indexOf(PARAM_START)).trim());
    }

    @NotNull
    private static List<String> extractCompositeInputs(@NotNull String criterion) {
        String params = criterion.substring(criterion.indexOf(COMPOSITE_START) + 1, criterion.lastIndexOf(COMPOSITE_END));

        Integer relevantCommaPosition = findSeparatingCommaPosition(params);
        List<String> result = Lists.newArrayList();
        if (relevantCommaPosition != null) {
            result.add(params.substring(0, relevantCommaPosition).trim());
            result.add(params.substring(relevantCommaPosition + 1).trim());
        } else {
            result.add(params.trim());
        }

        return result;
    }

    @Nullable
    private static Integer findSeparatingCommaPosition(@NotNull String params) {
        int nestedCompositeLevel = 0;
        int nestedParameterSection = 0;
        for (int i = 0; i < params.length(); i++) {
            char character = params.charAt(i);
            if (character == COMPOSITE_START) {
                nestedCompositeLevel++;
            } else if (character == COMPOSITE_END) {
                nestedCompositeLevel--;
            } else if (character == PARAM_START) {
                nestedParameterSection++;
            } else if (character == PARAM_END) {
                nestedParameterSection--;
            } else if (character == ',' && nestedCompositeLevel == 0 && nestedParameterSection == 0) {
                return i;
            }
        }

        return null;
    }

    @NotNull
    private static List<String> extractParameterizedInputs(@NotNull String criterion) {
        List<String> parameters = Lists.newArrayList();
        String parameterString = criterion.substring(criterion.indexOf(PARAM_START) + 1, criterion.lastIndexOf(PARAM_END));
        for (String parameter : parameterString.split(",")) {
            parameters.add(parameter.trim());
        }

        return parameters;
    }

    private static boolean isCompositeCriterion(@NotNull String criterion) {
        return criterion.contains(String.valueOf(COMPOSITE_START)) && criterion.endsWith(String.valueOf(COMPOSITE_END));
    }

    private static boolean isParameterizedCriterion(@NotNull String criterion) {
        return criterion.contains(String.valueOf(PARAM_START)) && criterion.endsWith(String.valueOf(PARAM_END));
    }
}
