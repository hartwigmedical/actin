package com.hartwig.actin.serve.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public final class ServeRules {

    private static final Set<EligibilityRule> MOLECULAR_RULES = Sets.newHashSet();

    static {
        MOLECULAR_RULES.add(EligibilityRule.ACTIVATION_OR_AMPLIFICATION_OF_GENE_X);
        MOLECULAR_RULES.add(EligibilityRule.INACTIVATION_OF_GENE_X);
        MOLECULAR_RULES.add(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X);
        MOLECULAR_RULES.add(EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y);
        MOLECULAR_RULES.add(EligibilityRule.AMPLIFICATION_OF_GENE_X);
        MOLECULAR_RULES.add(EligibilityRule.DELETION_OF_GENE_X);
        MOLECULAR_RULES.add(EligibilityRule.FUSION_IN_GENE_X);
        MOLECULAR_RULES.add(EligibilityRule.SPECIFIC_FUSION_OF_X_TO_Y);
        MOLECULAR_RULES.add(EligibilityRule.WILDTYPE_OF_GENE_X);
        MOLECULAR_RULES.add(EligibilityRule.MSI_SIGNATURE);
        MOLECULAR_RULES.add(EligibilityRule.HRD_SIGNATURE);
        MOLECULAR_RULES.add(EligibilityRule.TMB_OF_AT_LEAST_X);
        MOLECULAR_RULES.add(EligibilityRule.TML_OF_AT_LEAST_X);
        MOLECULAR_RULES.add(EligibilityRule.TML_OF_AT_MOST_X);
        MOLECULAR_RULES.add(EligibilityRule.HAS_HLA_A_TYPE_X);
    }

    private ServeRules() {
    }

    public static boolean isMolecular(@NotNull EligibilityRule rule) {
        return MOLECULAR_RULES.contains(rule);
    }
}
