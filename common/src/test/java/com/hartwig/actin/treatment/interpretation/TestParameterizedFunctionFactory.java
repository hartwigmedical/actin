package com.hartwig.actin.treatment.interpretation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;
import com.hartwig.actin.treatment.interpretation.composite.CompositeInput;
import com.hartwig.actin.treatment.interpretation.composite.CompositeRules;
import com.hartwig.actin.treatment.interpretation.single.FunctionInput;

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
            return createForInputs(FunctionInputResolver.RULE_INPUT_MAP.get(rule));
        }
    }

    @NotNull
    private static List<Object> createForInputs(@NotNull FunctionInput input) {
        switch (input) {
            case NONE: {
                return Lists.newArrayList();
            }
            case ONE_INTEGER:
            case ONE_DOUBLE: {
                return Lists.newArrayList("1");
            }
            case TWO_INTEGERS:
            case TWO_DOUBLES: {
                return Lists.newArrayList("1", "2");
            }
            case ONE_TREATMENT_CATEGORY: {
                return Lists.newArrayList(TreatmentCategoryResolver.toString(TreatmentCategory.IMMUNOTHERAPY));
            }
            case ONE_TREATMENT_CATEGORY_ONE_STRING: {
                return Lists.newArrayList(TreatmentCategoryResolver.toString(TreatmentCategory.IMMUNOTHERAPY), "string");
            }
            case ONE_TREATMENT_CATEGORY_ONE_INTEGER: {
                return Lists.newArrayList(TreatmentCategoryResolver.toString(TreatmentCategory.IMMUNOTHERAPY), "1");
            }
            case ONE_TREATMENT_CATEGORY_ONE_STRING_ONE_INTEGER: {
                return Lists.newArrayList(TreatmentCategoryResolver.toString(TreatmentCategory.IMMUNOTHERAPY), "string", "1");
            }
            case ONE_STRING: {
                return Lists.newArrayList("string");
            }
            case ONE_STRING_ONE_INTEGER: {
                return Lists.newArrayList("string", "1");
            }
            case ONE_STRING_TWO_INTEGERS: {
                return Lists.newArrayList("string", "1", "2");
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
            case ONE_TUMOR_STAGE: {
                return Lists.newArrayList(TumorStage.I.display());
            }
            default: {
                throw new IllegalStateException("Could not create inputs for " + input);
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
