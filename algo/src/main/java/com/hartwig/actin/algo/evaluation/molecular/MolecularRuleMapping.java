package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public final class MolecularRuleMapping {

    private MolecularRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.MOLECULAR_RESULTS_MUST_BE_AVAILABLE, molecularResultsAreAvailableCreator());
        map.put(EligibilityRule.MOLECULAR_RESULTS_MUST_BE_AVAILABLE_FOR_GENE_X, notImplementedCreator());
        map.put(EligibilityRule.ACTIVATION_OF_GENE_X, notImplementedCreator());
        map.put(EligibilityRule.INACTIVATION_OF_GENE_X, notImplementedCreator());
        map.put(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X, notImplementedCreator());
        map.put(EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y, notImplementedCreator());
        map.put(EligibilityRule.INACTIVATING_MUTATION_IN_GENE_X, notImplementedCreator());
        map.put(EligibilityRule.AMPLIFICATION_OF_GENE_X, notImplementedCreator());
        map.put(EligibilityRule.DELETION_OF_GENE_X, notImplementedCreator());
        map.put(EligibilityRule.ACTIVATING_FUSION_IN_GENE_X, notImplementedCreator());
        map.put(EligibilityRule.SPECIFIC_FUSION_X, notImplementedCreator());
        map.put(EligibilityRule.OVEREXPRESSION_OF_GENE_X, geneIsOverexpressedCreator());
        map.put(EligibilityRule.WILDTYPE_OF_GENE_X, notImplementedCreator());
        map.put(EligibilityRule.MSI_SIGNATURE, notImplementedCreator());
        map.put(EligibilityRule.HRD_SIGNATURE, notImplementedCreator());
        map.put(EligibilityRule.TMB_OF_AT_LEAST_X, notImplementedCreator());
        map.put(EligibilityRule.TML_OF_AT_LEAST_X, notImplementedCreator());
        map.put(EligibilityRule.TML_OF_AT_MOST_X, notImplementedCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator molecularResultsAreAvailableCreator() {
        return function -> new MolecularResultsAreAvailable();
    }

    @NotNull
    private static FunctionCreator geneIsOverexpressedCreator() {
        return function -> new GeneIsOverexpressed();
    }

    @NotNull
    private static FunctionCreator notImplementedCreator() {
        return function -> evaluation -> Evaluation.NOT_IMPLEMENTED;
    }
}
