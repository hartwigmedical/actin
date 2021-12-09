package com.hartwig.actin.treatment.interpretation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;

import org.jetbrains.annotations.NotNull;

public final class TestParameterizedFunctionFactory {

    private static final EligibilityRule MOCK_RULE = firstNonComposite();

    private TestParameterizedFunctionFactory() {
    }

    @NotNull
    public static EligibilityFunction create(@NotNull EligibilityRule rule) {
        return ImmutableEligibilityFunction.builder().rule(rule).parameters(createTestParameters(rule)).build();
    }

    @NotNull
    private static List<Object> createTestParameters(@NotNull EligibilityRule rule) {
        if (CompositeRules.isComposite(rule)) {
            CompositeInput inputs = CompositeRules.inputsForCompositeRule(rule);
            if (inputs == CompositeInput.EXACTLY_1) {
                return Lists.newArrayList(create(MOCK_RULE));
            } else if (inputs == CompositeInput.AT_LEAST_2) {
                return Lists.newArrayList(create(MOCK_RULE), create(MOCK_RULE));
            } else {
                throw new IllegalStateException("Cannot interpret composite input: " + inputs);
            }
        } else {
            return createForInputs(EligibilityParameterResolver.PARAMETER_MAP.get(rule));
        }
    }

    @NotNull
    private static List<Object> createForInputs(@NotNull RuleInput ruleInput) {
        switch (ruleInput) {
            case NONE: {
                return Lists.newArrayList();
            }
            case ONE_INTEGER:
            case ONE_DOUBLE: {
                return Lists.newArrayList("1");
            }
            case ONE_TREATMENT_CATEGORY: {
                return Lists.newArrayList(TreatmentCategoryResolver.toString(TreatmentCategory.IMMUNOTHERAPY));
            }
            case ONE_TREATMENT_CATEGORY_ONE_STRING: {
                return Lists.newArrayList(TreatmentCategoryResolver.toString(TreatmentCategory.IMMUNOTHERAPY), "string");
            }
            case ONE_STRING: {
                return Lists.newArrayList("string");
            }
            case TWO_STRINGS: {
                return Lists.newArrayList("string", "string");
            }
            case ONE_INTEGER_ONE_STRING: {
                return Lists.newArrayList("1", "string");
            }
            case ONE_INTEGER_MANY_STRINGS: {
                return Lists.newArrayList("1", "string1;string2");
            }
            default: {
                throw new IllegalStateException("Could not create inputs for " + ruleInput);
            }
        }
    }

    @NotNull
    private static EligibilityRule firstNonComposite() {
        for (EligibilityRule rule : EligibilityRule.values()) {
            if (!CompositeRules.isComposite(rule)) {
                return rule;
            }
        }

        throw new IllegalStateException("Only composite functions defined!");
    }
}
