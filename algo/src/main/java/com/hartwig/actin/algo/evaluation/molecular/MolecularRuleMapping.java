package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.FunctionInputResolver;
import com.hartwig.actin.treatment.input.single.OneIntegerOneString;
import com.hartwig.actin.treatment.input.single.TwoStrings;

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
        map.put(EligibilityRule.FUSION_IN_GENE_X, hasFusionInGeneCreator());
        map.put(EligibilityRule.WILDTYPE_OF_GENE_X, geneIsWildtypeCreator());
        map.put(EligibilityRule.MSI_SIGNATURE, isMicrosatelliteUnstableCreator());
        map.put(EligibilityRule.HRD_SIGNATURE, isHomologousRepairDeficientCreator());
        map.put(EligibilityRule.TMB_OF_AT_LEAST_X, hasSufficientTumorMutationalBurdenCreator());
        map.put(EligibilityRule.TML_OF_AT_LEAST_X, hasSufficientTumorMutationalLoadCreator());
        map.put(EligibilityRule.TML_OF_AT_MOST_X, hasLimitedTumorMutationalLoadCreator());
        map.put(EligibilityRule.HAS_HLA_A_TYPE_X, hasSpecificHLATypeCreator());
        map.put(EligibilityRule.OVEREXPRESSION_OF_GENE_X, geneIsOverexpressedCreator());
        map.put(EligibilityRule.NON_EXPRESSION_OF_GENE_X, geneIsNotExpressedCreator());
        map.put(EligibilityRule.EXPRESSION_OF_GENE_X_BY_IHC, geneIsExpressedByIHCCreator());
        map.put(EligibilityRule.EXPRESSION_OF_GENE_X_BY_IHC_OF_EXACTLY_Y, geneHasExactExpressionByIHCCreator());
        map.put(EligibilityRule.EXPRESSION_OF_GENE_X_BY_IHC_OF_AT_LEAST_Y, geneHasSufficientExpressionByIHCCreator());
        map.put(EligibilityRule.PD_L1_SCORE_CPS_OF_AT_LEAST_X, hasSufficientPDL1ByIHCCreator());
        map.put(EligibilityRule.PD_L1_SCORE_CPS_OF_AT_MOST_X, hasLimitedPDL1ByCPSByIHCCreator());
        map.put(EligibilityRule.PD_L1_SCORE_TPS_OF_AT_MOST_X, hasLimitedPDL1ByTPSByIHCCreator());
        map.put(EligibilityRule.HAS_PSMA_POSITIVE_PET_SCAN, hasPSMAPositivePETScanCreator());
        map.put(EligibilityRule.MANUFACTURED_T_CELLS_ARE_WITHIN_SHELF_LIFE, manufacturedTCellsWithinShelfLifeCreator());

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
            TwoStrings inputs = FunctionInputResolver.createTwoStringsInput(function);
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
    private static FunctionCreator hasFusionInGeneCreator() {
        return function -> {
            String gene = FunctionInputResolver.createOneStringInput(function);
            return new HasFusionInGene(gene);
        };
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

    @NotNull
    private static FunctionCreator hasSpecificHLATypeCreator() {
        return function -> new HasSpecificHLAType();
    }

    @NotNull
    private static FunctionCreator geneIsOverexpressedCreator() {
        return function -> new GeneIsOverexpressed();
    }

    @NotNull
    private static FunctionCreator geneIsNotExpressedCreator() {
        return function -> new GeneIsNotExpressed();
    }

    @NotNull
    private static FunctionCreator geneIsExpressedByIHCCreator() {
        return function -> {
            String gene = FunctionInputResolver.createOneStringInput(function);
            return new GeneIsExpressedByIHC(gene);
        };
    }

    @NotNull
    private static FunctionCreator geneHasExactExpressionByIHCCreator() {
        return function -> {
            OneIntegerOneString input = FunctionInputResolver.createOneStringOneIntegerInput(function);
            return new GeneHasExactExpressionByIHC(input.string(), input.integer());
        };
    }

    @NotNull
    private static FunctionCreator geneHasSufficientExpressionByIHCCreator() {
        return function -> {
            OneIntegerOneString input = FunctionInputResolver.createOneStringOneIntegerInput(function);
            return new GeneHasSufficientExpressionByIHC(input.string(), input.integer());
        };
    }

    @NotNull
    private static FunctionCreator hasSufficientPDL1ByIHCCreator() {
        return function -> {
            int minPDL1 = FunctionInputResolver.createOneIntegerInput(function);
            return new HasSufficientPDL1ByIHC(minPDL1);
        };
    }

    @NotNull
    private static FunctionCreator hasLimitedPDL1ByCPSByIHCCreator() {
        return function -> {
            int maxPDL1 = FunctionInputResolver.createOneIntegerInput(function);
            return new HasLimitedPDL1ByCPSByIHC(maxPDL1);
        };
    }

    @NotNull
    private static FunctionCreator hasLimitedPDL1ByTPSByIHCCreator() {
        return function -> new HasLimitedPDL1ByTPSByIHC();
    }

    @NotNull
    private static FunctionCreator hasPSMAPositivePETScanCreator() { return function -> new HasPSMAPositivePETScan(); }

    @NotNull
    private static FunctionCreator manufacturedTCellsWithinShelfLifeCreator() {
        return function -> new ManufacturedTCellsWithinShelfLife();
    }
}
