package com.hartwig.actin.serve.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ServeExtraction {

    static final Set<EligibilityRule> RULES_WITH_GENE_AS_FIRST_PARAM = Sets.newHashSet();

    private ServeExtraction() {
    }

    static {
        RULES_WITH_GENE_AS_FIRST_PARAM.add(EligibilityRule.ACTIVATION_OR_AMPLIFICATION_OF_GENE_X);
        RULES_WITH_GENE_AS_FIRST_PARAM.add(EligibilityRule.INACTIVATION_OF_GENE_X);
        RULES_WITH_GENE_AS_FIRST_PARAM.add(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X);
        RULES_WITH_GENE_AS_FIRST_PARAM.add(EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y);
        RULES_WITH_GENE_AS_FIRST_PARAM.add(EligibilityRule.AMPLIFICATION_OF_GENE_X);
        RULES_WITH_GENE_AS_FIRST_PARAM.add(EligibilityRule.DELETION_OF_GENE_X);
        RULES_WITH_GENE_AS_FIRST_PARAM.add(EligibilityRule.FUSION_IN_GENE_X);
        RULES_WITH_GENE_AS_FIRST_PARAM.add(EligibilityRule.WILDTYPE_OF_GENE_X);
    }

    @Nullable
    public static String gene(@NotNull EligibilityFunction function) {
        if (!ServeRules.isMolecular(function.rule())) {
            throw new IllegalArgumentException("Not a molecular rule: " + function);
        }

        if (!RULES_WITH_GENE_AS_FIRST_PARAM.contains(function.rule())) {
            return null;
        }

        if (function.parameters().isEmpty()) {
            throw new IllegalStateException("Cannot extract gene for molecular function without parameters: " + function);
        }

        return (String) function.parameters().get(0);
    }

    @Nullable
    public static String mutation(@NotNull EligibilityFunction function) {
        if (!ServeRules.isMolecular(function.rule())) {
            throw new IllegalArgumentException("Not a molecular rule: " + function);
        }

        switch (function.rule()) {
            case MUTATION_IN_GENE_X_OF_TYPE_Y: {
                if (function.parameters().size() != 2) {
                    throw new IllegalStateException("Cannot determine mutation for rule: " + function);
                }
                return (String) function.parameters().get(1);
            }
            case MSI_SIGNATURE: {
                return "MSI high";
            } case HRD_SIGNATURE: {
                return "HRD pos";
            } case TMB_OF_AT_LEAST_X: {
                if (function.parameters().size() != 1) {
                    throw new IllegalStateException("Cannot determine TMB cutoff for rule: " + function);
                }
                return "TMB >= " + function.parameters().get(0);
            } case TML_OF_AT_LEAST_X: {
                if (function.parameters().size() != 1) {
                    throw new IllegalStateException("Cannot determine TML cutoff for rule: " + function);
                }
                return "TML >= " + function.parameters().get(0);
            } case TML_OF_AT_MOST_X: {
                if (function.parameters().size() != 1) {
                    throw new IllegalStateException("Cannot determine TML cutoff for rule: " + function);
                }
                return "TML <= " + function.parameters().get(0);
            } case HAS_HLA_A_TYPE_X: {
                if (function.parameters().size() != 1) {
                    throw new IllegalStateException("Cannot determine required HLA type for rule: " + function);
                }
                return (String) function.parameters().get(0);
            }
            default: {
                return null;
            }
        }
    }
}
