package com.hartwig.actin.treatment.input;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;
import com.hartwig.actin.treatment.input.composite.CompositeInput;
import com.hartwig.actin.treatment.input.composite.CompositeRules;
import com.hartwig.actin.treatment.input.datamodel.TreatmentInput;
import com.hartwig.actin.treatment.input.datamodel.TumorTypeInput;
import com.hartwig.actin.treatment.input.single.FunctionInput;

import org.jetbrains.annotations.NotNull;

public final class ParameterizedFunctionTestFactory {

    private static final EligibilityRule MOCK_RULE = firstNonComposite();

    private ParameterizedFunctionTestFactory() {
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
            return createForInputs(FunctionInputMapping.RULE_INPUT_MAP.get(rule));
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
            case ONE_TREATMENT: {
                return Lists.newArrayList(TreatmentInput.IMMUNOTHERAPY.display());
            }
            case ONE_TYPED_TREATMENT_MANY_STRINGS: {
                return Lists.newArrayList(TreatmentInput.IMMUNOTHERAPY.display(), "string1;string2");
            }
            case ONE_TREATMENT_ONE_INTEGER: {
                return Lists.newArrayList(TreatmentInput.IMMUNOTHERAPY.display(), "1");
            }
            case ONE_TYPED_TREATMENT_MANY_STRINGS_ONE_INTEGER: {
                return Lists.newArrayList(TreatmentInput.IMMUNOTHERAPY.display(), "string1;string2", "1");
            }
            case ONE_TUMOR_TYPE: {
                return Lists.newArrayList(TumorTypeInput.SQUAMOUS_CELL_CARCINOMA.display());
            }
            case ONE_STRING: {
                return Lists.newArrayList("string");
            }
            case ONE_STRING_ONE_INTEGER: {
                return Lists.newArrayList("string", "1");
            }
            case TWO_STRINGS: {
                return Lists.newArrayList("string", "string");
            }
            case MANY_STRINGS_ONE_INTEGER: {
                return Lists.newArrayList("string1;string2", "1");
            }
            case MANY_STRINGS_TWO_INTEGERS: {
                return Lists.newArrayList("string1;string2", "1", "2");
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
            case ONE_HLA_ALLELE: {
                return Lists.newArrayList("A*02:01");
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
