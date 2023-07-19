package com.hartwig.actin.treatment.input;

import java.util.Collections;
import java.util.List;

import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;
import com.hartwig.actin.treatment.input.composite.CompositeInput;
import com.hartwig.actin.treatment.input.composite.CompositeRules;
import com.hartwig.actin.treatment.input.datamodel.TreatmentCategoryInput;
import com.hartwig.actin.treatment.input.datamodel.TumorTypeInput;
import com.hartwig.actin.treatment.input.single.FunctionInput;

import org.jetbrains.annotations.NotNull;

public class ParameterizedFunctionTestFactory {

    private static final EligibilityRule MOCK_RULE = firstNonComposite();

    @NotNull
    private final String doidTermToUse;

    public ParameterizedFunctionTestFactory(@NotNull final String doidTermToUse) {
        this.doidTermToUse = doidTermToUse;
    }

    @NotNull
    public EligibilityFunction create(@NotNull EligibilityRule rule) {
        return ImmutableEligibilityFunction.builder().rule(rule).parameters(createTestParameters(rule)).build();
    }

    @NotNull
    private List<Object> createTestParameters(@NotNull EligibilityRule rule) {
        if (CompositeRules.isComposite(rule)) {
            CompositeInput inputs = CompositeRules.inputsForCompositeRule(rule);
            if (inputs == CompositeInput.EXACTLY_1) {
                return List.of(create(MOCK_RULE));
            } else if (inputs == CompositeInput.AT_LEAST_2) {
                return List.of(create(MOCK_RULE), create(MOCK_RULE));
            } else {
                throw new IllegalStateException("Cannot interpret composite input: " + inputs);
            }
        } else {
            return createForInputs(FunctionInputMapping.RULE_INPUT_MAP.get(rule));
        }
    }

    @NotNull
    private List<Object> createForInputs(@NotNull FunctionInput input) {
        switch (input) {
            case NONE: {
                return Collections.emptyList();
            }
            case ONE_INTEGER:
            case ONE_DOUBLE: {
                return List.of("1");
            }
            case MANY_INTEGERS:
                return List.of("1;2");
            case TWO_INTEGERS:
            case TWO_DOUBLES: {
                return List.of("1", "2");
            }
            case ONE_TREATMENT_CATEGORY_OR_TYPE: {
                return List.of(TreatmentCategoryInput.fromString(TreatmentCategory.IMMUNOTHERAPY.display()));
            }
            case ONE_TREATMENT_CATEGORY_MANY_TYPES: {
                return List.of(TreatmentCategoryInput.fromString(TreatmentCategory.IMMUNOTHERAPY.display()), "string1;string2");
            }
            case ONE_TREATMENT_CATEGORY_OR_TYPE_ONE_INTEGER: {
                return List.of(TreatmentCategoryInput.fromString(TreatmentCategory.IMMUNOTHERAPY.display()), "1");
            }
            case ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER: {
                return List.of(TreatmentCategoryInput.fromString(TreatmentCategory.IMMUNOTHERAPY.display()), "string1;string2", "1");
            }
            case ONE_TUMOR_TYPE: {
                return List.of(TumorTypeInput.SQUAMOUS_CELL_CARCINOMA.display());
            }
            case ONE_STRING: {
                return List.of("string");
            }
            case ONE_STRING_ONE_INTEGER: {
                return List.of("string", "1");
            }
            case MANY_STRINGS_ONE_INTEGER: {
                return List.of("string1;string2", "1");
            }
            case MANY_STRINGS_TWO_INTEGERS: {
                return List.of("string1;string2", "1", "2");
            }
            case ONE_INTEGER_ONE_STRING: {
                return List.of("1", "string");
            }
            case ONE_INTEGER_MANY_STRINGS: {
                return List.of("1", "string1;string2");
            }
            case ONE_TUMOR_STAGE: {
                return List.of(TumorStage.I.display());
            }
            case ONE_HLA_ALLELE: {
                return List.of("A*02:01");
            }
            case ONE_GENE: {
                return List.of("gene");
            }
            case ONE_GENE_ONE_INTEGER: {
                return List.of("gene", "1");
            }
            case ONE_GENE_ONE_INTEGER_ONE_VARIANT_TYPE: {
                return List.of("gene", "1", "INDEL");
            }
            case ONE_GENE_TWO_INTEGERS: {
                return List.of("gene", "1", "2");
            }
            case ONE_GENE_MANY_CODONS: {
                return List.of("gene", "V600;V601");
            }
            case ONE_GENE_MANY_PROTEIN_IMPACTS: {
                return List.of("gene", "V600E;V601E");
            }
            case MANY_GENES: {
                return List.of("gene1;gene2");
            }
            case ONE_DOID_TERM: {
                return List.of(doidTermToUse);
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
