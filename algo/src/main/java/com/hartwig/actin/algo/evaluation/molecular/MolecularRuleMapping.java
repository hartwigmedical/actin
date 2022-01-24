package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class MolecularRuleMapping {

    private MolecularRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.MOLECULAR_RESULTS_MUST_BE_AVAILABLE, molecularResultsAreAvailableCreator());
        map.put(EligibilityRule.MOLECULAR_RESULTS_MUST_BE_AVAILABLE_FOR_GENE_X, molecularResultsAreAvailableCreator());
        map.put(EligibilityRule.ACTIVATION_OR_AMPLIFICATION_OF_GENE_X, geneIsActivatedOrAmplifiedCreator());
        map.put(EligibilityRule.INACTIVATION_OF_GENE_X, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.AMPLIFICATION_OF_GENE_X, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.DELETION_OF_GENE_X, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.ACTIVATING_FUSION_IN_GENE_X, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.SPECIFIC_FUSION_X, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.OVEREXPRESSION_OF_GENE_X, geneIsOverexpressedCreator());
        map.put(EligibilityRule.EXPRESSION_OF_GENE_X_BY_IHC, geneIsExpressedByIHCCreator());
        map.put(EligibilityRule.EXPRESSION_OF_GENE_X_BY_IHC_OF_AT_LEAST_Y, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.WILDTYPE_OF_GENE_X, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.MSI_SIGNATURE, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.HRD_SIGNATURE, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.TMB_OF_AT_LEAST_X, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.TML_OF_AT_LEAST_X, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.TML_OF_AT_MOST_X, function -> record -> Evaluation.NOT_IMPLEMENTED);

        return map;
    }

    @NotNull
    private static FunctionCreator molecularResultsAreAvailableCreator() {
        return function -> new MolecularResultsAreAvailable();
    }

    @NotNull
    private static FunctionCreator geneIsActivatedOrAmplifiedCreator() {
        return function -> {
            String gene = FunctionInputResolver.createOneStringInput(function);
            return new GeneIsActivatedOrAmplified(gene);
        };
    }

    @NotNull
    private static FunctionCreator geneIsOverexpressedCreator() {
        return function -> new GeneIsOverexpressed();
    }

    @NotNull
    private static FunctionCreator geneIsExpressedByIHCCreator() {
        return function -> new GeneIsExpressedByIHC();
    }

}
