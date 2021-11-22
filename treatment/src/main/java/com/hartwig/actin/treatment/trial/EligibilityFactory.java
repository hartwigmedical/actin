package com.hartwig.actin.treatment.trial;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;
import com.hartwig.actin.treatment.interpretation.CompositeRules;
import com.hartwig.actin.treatment.interpretation.EligibilityParameterResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

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
        Boolean hasValidParameters = EligibilityParameterResolver.hasValidParameters(function);
        if (hasValidParameters == null || !hasValidParameters) {
            throw new IllegalStateException("Function " + function.rule() + " has invalid parameters: '" + function.parameters() + "'");
        }
        return function;
    }

    @NotNull
    private static EligibilityRule extractCompositeRule(@NotNull String criterion) {
        EligibilityRule rule = EligibilityRule.valueOf(criterion.substring(0, criterion.indexOf(COMPOSITE_START)));
        if (!CompositeRules.isComposite(rule)) {
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

        List<Integer> relevantCommaPositions = findSeparatingCommaPositions(params);
        if (relevantCommaPositions.isEmpty()) {
            return Lists.newArrayList(params.trim());
        }

        List<String> result = Lists.newArrayList();

        int index = 0;
        while (index < relevantCommaPositions.size()) {
            int start = index == 0 ? -1 : relevantCommaPositions.get(index - 1);
            result.add(params.substring(start + 1, relevantCommaPositions.get(index)).trim());
            index++;
        }
        result.add(params.substring(relevantCommaPositions.get(relevantCommaPositions.size() - 1) + 1).trim());

        return result;
    }

    @NotNull
    private static List<Integer> findSeparatingCommaPositions(@NotNull String params) {
        int nestedCompositeLevel = 0;
        int nestedParameterSection = 0;

        List<Integer> commaPositions = Lists.newArrayList();
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
                commaPositions.add(i);
            }
        }

        return commaPositions;
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
