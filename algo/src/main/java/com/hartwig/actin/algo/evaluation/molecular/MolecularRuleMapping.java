package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.FunctionInputResolver;
import com.hartwig.actin.treatment.interpretation.single.TwoStrings;

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
        map.put(EligibilityRule.INACTIVATION_OF_GENE_X, geneIsInactivatedCreator());
        map.put(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X, geneHasActivatingMutationCreator());
        map.put(EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y, geneHasSpecificMutationCreator());
        map.put(EligibilityRule.AMPLIFICATION_OF_GENE_X, geneIsAmplifiedCreator());
        map.put(EligibilityRule.DELETION_OF_GENE_X, geneIsDeletedCreator());
        map.put(EligibilityRule.ACTIVATING_FUSION_IN_GENE_X, hasActivatingFusionInGeneCreator());
        map.put(EligibilityRule.SPECIFIC_FUSION_OF_X_TO_Y, hasSpecificFusionCreator());
        map.put(EligibilityRule.OVEREXPRESSION_OF_GENE_X, geneIsOverexpressedCreator());
        map.put(EligibilityRule.EXPRESSION_OF_GENE_X_BY_IHC, geneIsExpressedByIHCCreator());
        map.put(EligibilityRule.EXPRESSION_OF_GENE_X_BY_IHC_OF_AT_LEAST_Y, geneIsExpressedByIHCCreator());
        map.put(EligibilityRule.WILDTYPE_OF_GENE_X, geneIsWildtypeCreator());
        map.put(EligibilityRule.MSI_SIGNATURE, isMicrosatelliteUnstableCreator());
        map.put(EligibilityRule.HRD_SIGNATURE, isHomologousRepairDeficientCreator());
        map.put(EligibilityRule.TMB_OF_AT_LEAST_X, hasSufficientTumorMutationalBurdenCreator());
        map.put(EligibilityRule.TML_OF_AT_LEAST_X, hasSufficientTumorMutationalLoadCreator());
        map.put(EligibilityRule.TML_OF_AT_MOST_X, hasLimitedTumorMutationalLoadCreator());

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
    private static FunctionCreator geneIsInactivatedCreator() {
        return function -> {
            String gene = FunctionInputResolver.createOneStringInput(function);
            return new GeneIsInactivated(gene);
        };
    }

    @NotNull
    private static FunctionCreator geneHasActivatingMutationCreator() {
        return function -> {
            String gene = FunctionInputResolver.createOneStringInput(function);
            return new GeneHasActivatingMutation(gene);
        };
    }

    @NotNull
    private static FunctionCreator geneHasSpecificMutationCreator() {
        return function -> {
            TwoStrings inputs = FunctionInputResolver.createTwoStringInput(function);
            return new GeneHasSpecificMutation(inputs.string1(), inputs.string2());
        };
    }

    @NotNull
    private static FunctionCreator geneIsAmplifiedCreator() {
        return function -> {
            String gene = FunctionInputResolver.createOneStringInput(function);
            return new GeneIsAmplified(gene);
        };
    }

    @NotNull
    private static FunctionCreator geneIsDeletedCreator() {
        return function -> {
            String gene = FunctionInputResolver.createOneStringInput(function);
            return new GeneIsDeleted(gene);
        };
    }

    @NotNull
    private static FunctionCreator hasActivatingFusionInGeneCreator() {
        return function -> {
            String gene = FunctionInputResolver.createOneStringInput(function);
            return new HasActivatingFusionWithGene(gene);
        };
    }

    @NotNull
    private static FunctionCreator hasSpecificFusionCreator() {
        return function -> {
            TwoStrings genes = FunctionInputResolver.createTwoStringInput(function);
            return new HasSpecificFusionGene(genes.string1(), genes.string2());
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

    @NotNull
    private static FunctionCreator geneIsWildtypeCreator() {
        return function -> {
            String gene = FunctionInputResolver.createOneStringInput(function);
            return new GeneIsWildtype(gene);
        };
    }

    @NotNull
    private static FunctionCreator isMicrosatelliteUnstableCreator() {
        return function -> new IsMicrosatelliteUnstable();
    }

    @NotNull
    private static FunctionCreator isHomologousRepairDeficientCreator() {
        return function -> new IsHomologousRepairDeficient();
    }

    @NotNull
    private static FunctionCreator hasSufficientTumorMutationalBurdenCreator() {
        return function -> {
            double minTumorMutationalBurden = FunctionInputResolver.createOneDoubleInput(function);
            return new HasSufficientTumorMutationalBurden(minTumorMutationalBurden);
        };
    }

    @NotNull
    private static FunctionCreator hasSufficientTumorMutationalLoadCreator() {
        return function -> {
            int minTumorMutationalLoad = FunctionInputResolver.createOneIntegerInput(function);
            return new HasSufficientTumorMutationalLoad(minTumorMutationalLoad);
        };
    }

    @NotNull
    private static FunctionCreator hasLimitedTumorMutationalLoadCreator() {
        return function -> {
            int maxTumorMutationalLoad = FunctionInputResolver.createOneIntegerInput(function);
            return new HasLimitedTumorMutationalLoad(maxTumorMutationalLoad);
        };
    }
}
