package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.RuleMapper;
import com.hartwig.actin.algo.evaluation.RuleMappingResources;
import com.hartwig.actin.algo.evaluation.composite.Or;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.single.OneIntegerOneString;
import com.hartwig.actin.treatment.input.single.OneIntegerOneStringOneVariantType;
import com.hartwig.actin.treatment.input.single.OneStringManyStrings;
import com.hartwig.actin.treatment.input.single.TwoIntegers;
import com.hartwig.actin.treatment.input.single.TwoIntegersOneString;

import org.jetbrains.annotations.NotNull;

public class MolecularRuleMapper extends RuleMapper {

    public MolecularRuleMapper(@NotNull final RuleMappingResources resources) {
        super(resources);
    }

    @NotNull
    @Override
    public Map<EligibilityRule, FunctionCreator> createMappings() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.ACTIVATION_OR_AMPLIFICATION_OF_GENE_X, geneIsActivatedOrAmplifiedCreator());
        map.put(EligibilityRule.INACTIVATION_OF_GENE_X, geneIsInactivatedCreator());
        map.put(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X, geneHasActivatingMutationCreator());
        map.put(EligibilityRule.MUTATION_IN_GENE_X_OF_ANY_PROTEIN_IMPACTS_Y, geneHasVariantWithAnyProteinImpactsCreator());
        map.put(EligibilityRule.MUTATION_IN_GENE_X_OF_ANY_CODING_IMPACTS_Y, geneHasVariantWithAnyCodingImpactsCreator());
        map.put(EligibilityRule.MUTATION_IN_GENE_X_IN_ANY_CODONS_Y, geneHasVariantInAnyCodonsCreator());
        map.put(EligibilityRule.MUTATION_IN_GENE_X_IN_EXON_Y, geneHasVariantInExonCreator());
        map.put(EligibilityRule.MUTATION_IN_GENE_X_IN_EXON_Y_TO_EXON_Z, geneHasVariantInExonRangeCreator());
        map.put(EligibilityRule.MUTATION_IN_GENE_X_IN_EXON_Y_OF_TYPE_Z, geneHasVariantInExonOfTypeCreator());
        map.put(EligibilityRule.UTR_3_LOSS_IN_GENE_X, geneHasUTR3LossCreator());
        map.put(EligibilityRule.AMPLIFICATION_OF_GENE_X, geneIsAmplifiedCreator());
        map.put(EligibilityRule.FUSION_IN_GENE_X, hasFusionInGeneCreator());
        map.put(EligibilityRule.WILDTYPE_OF_GENE_X, geneIsWildTypeCreator());
        map.put(EligibilityRule.EXON_SKIPPING_GENE_X_EXON_Y, geneHasSpecificExonSkippingCreator());
        map.put(EligibilityRule.MSI_SIGNATURE, isMicrosatelliteUnstableCreator());
        map.put(EligibilityRule.HRD_SIGNATURE, isHomologousRepairDeficientCreator());
        map.put(EligibilityRule.TMB_OF_AT_LEAST_X, hasSufficientTumorMutationalBurdenCreator());
        map.put(EligibilityRule.TML_OF_AT_LEAST_X, hasSufficientTumorMutationalLoadCreator());
        map.put(EligibilityRule.TML_BETWEEN_X_AND_Y, hasCertainTumorMutationalLoadCreator());
        map.put(EligibilityRule.HAS_HLA_TYPE_X, hasSpecificHLATypeCreator());
        map.put(EligibilityRule.OVEREXPRESSION_OF_GENE_X, geneIsOverexpressedCreator());
        map.put(EligibilityRule.NON_EXPRESSION_OF_GENE_X, geneIsNotExpressedCreator());
        map.put(EligibilityRule.EXPRESSION_OF_GENE_X_BY_IHC, geneIsExpressedByIHCCreator());
        map.put(EligibilityRule.EXPRESSION_OF_GENE_X_BY_IHC_OF_EXACTLY_Y, geneHasExactExpressionByIHCCreator());
        map.put(EligibilityRule.EXPRESSION_OF_GENE_X_BY_IHC_OF_AT_LEAST_Y, geneHasSufficientExpressionByIHCCreator());
        map.put(EligibilityRule.PD_L1_SCORE_CPS_OF_AT_LEAST_X, hasSufficientPDL1ByCPSByIHCCreator());
        map.put(EligibilityRule.PD_L1_SCORE_CPS_OF_AT_MOST_X, hasLimitedPDL1ByCPSByIHCCreator());
        map.put(EligibilityRule.PD_L1_SCORE_TPS_OF_AT_MOST_X, hasLimitedPDL1ByTPSByIHCCreator());
        map.put(EligibilityRule.PD_L1_STATUS_MUST_BE_AVAILABLE, hasAvailablePDL1StatusCreator());
        map.put(EligibilityRule.HAS_PSMA_POSITIVE_PET_SCAN, hasPSMAPositivePETScanCreator());
        map.put(EligibilityRule.MOLECULAR_RESULTS_MUST_BE_AVAILABLE, molecularResultsAreGenerallyAvailableCreator());
        map.put(EligibilityRule.MOLECULAR_TEST_MUST_HAVE_BEEN_DONE_FOR_GENE_X, molecularResultsAreAvailableForGeneCreator());
        map.put(EligibilityRule.MOLECULAR_TEST_MUST_HAVE_BEEN_DONE_FOR_PROMOTER_OF_GENE_X,
                molecularResultsAreAvailableForPromoterOfGeneCreator());

        return map;
    }

    @NotNull
    private FunctionCreator geneIsActivatedOrAmplifiedCreator() {
        return function -> {
            String gene = functionInputResolver().createOneStringInput(function);
            return new Or(Lists.newArrayList(new GeneHasActivatingMutation(gene), new GeneIsAmplified(gene)));
        };
    }

    @NotNull
    private FunctionCreator geneIsInactivatedCreator() {
        return function -> {
            String gene = functionInputResolver().createOneStringInput(function);
            return new GeneIsInactivated(gene);
        };
    }

    @NotNull
    private FunctionCreator geneHasActivatingMutationCreator() {
        return function -> {
            String gene = functionInputResolver().createOneStringInput(function);
            return new GeneHasActivatingMutation(gene);
        };
    }

    @NotNull
    private FunctionCreator geneHasVariantWithAnyProteinImpactsCreator() {
        return function -> {
            OneStringManyStrings input = functionInputResolver().createOneStringManyStringsInput(function);
            return new GeneHasVariantWithProteinImpact(input.string1(), input.additionalStrings());
        };
    }

    @NotNull
    private FunctionCreator geneHasVariantWithAnyCodingImpactsCreator() {
        return function -> {
            OneStringManyStrings input = functionInputResolver().createOneStringManyStringsInput(function);
            return new GeneHasVariantWithCodingImpact(input.string1(), input.additionalStrings());
        };
    }

    @NotNull
    private FunctionCreator geneHasVariantInAnyCodonsCreator() {
        return function -> {
            OneStringManyStrings input = functionInputResolver().createOneStringManyStringsInput(function);
            return new GeneHasVariantInCodon(input.string1(), input.additionalStrings());
        };
    }

    @NotNull
    private FunctionCreator geneHasVariantInExonCreator() {
        return function -> {
            OneIntegerOneString input = functionInputResolver().createOneStringOneIntegerInput(function);
            return new GeneHasVariantInExonRangeOfType(input.string(), input.integer(), input.integer(), null);
        };
    }

    @NotNull
    private FunctionCreator geneHasVariantInExonRangeCreator() {
        return function -> {
            TwoIntegersOneString input = functionInputResolver().createOneStringTwoIntegersInput(function);
            return new GeneHasVariantInExonRangeOfType(input.string(), input.integer1(), input.integer2(), null);
        };
    }

    @NotNull
    private FunctionCreator geneHasVariantInExonOfTypeCreator() {
        return function -> {
            OneIntegerOneStringOneVariantType input = functionInputResolver().createOneStringOneIntegerOneVariantTypeInput(function);
            return new GeneHasVariantInExonRangeOfType(input.string(), input.integer(), input.integer(), input.variantType());
        };
    }

    @NotNull
    private FunctionCreator geneHasUTR3LossCreator() {
        return function -> {
            String gene = functionInputResolver().createOneStringInput(function);
            return new GeneHasUTR3Loss(gene);
        };
    }

    @NotNull
    private FunctionCreator geneIsAmplifiedCreator() {
        return function -> {
            String gene = functionInputResolver().createOneStringInput(function);
            return new GeneIsAmplified(gene);
        };
    }

    @NotNull
    private FunctionCreator hasFusionInGeneCreator() {
        return function -> {
            String gene = functionInputResolver().createOneStringInput(function);
            return new HasFusionInGene(gene);
        };
    }

    @NotNull
    private FunctionCreator geneIsWildTypeCreator() {
        return function -> {
            String gene = functionInputResolver().createOneStringInput(function);
            return new GeneIsWildType(gene);
        };
    }

    @NotNull
    private FunctionCreator geneHasSpecificExonSkippingCreator() {
        return function -> {
            OneIntegerOneString input = functionInputResolver().createOneStringOneIntegerInput(function);
            return new GeneHasSpecificExonSkipping(input.string(), input.integer());
        };
    }

    @NotNull
    private FunctionCreator isMicrosatelliteUnstableCreator() {
        return function -> new IsMicrosatelliteUnstable();
    }

    @NotNull
    private FunctionCreator isHomologousRepairDeficientCreator() {
        return function -> new IsHomologousRepairDeficient();
    }

    @NotNull
    private FunctionCreator hasSufficientTumorMutationalBurdenCreator() {
        return function -> {
            double minTumorMutationalBurden = functionInputResolver().createOneDoubleInput(function);
            return new HasSufficientTumorMutationalBurden(minTumorMutationalBurden);
        };
    }

    @NotNull
    private FunctionCreator hasSufficientTumorMutationalLoadCreator() {
        return function -> {
            int minTumorMutationalLoad = functionInputResolver().createOneIntegerInput(function);
            return new HasTumorMutationalLoadWithinRange(minTumorMutationalLoad, null);
        };
    }

    @NotNull
    private FunctionCreator hasCertainTumorMutationalLoadCreator() {
        return function -> {
            TwoIntegers input = functionInputResolver().createTwoIntegersInput(function);
            return new HasTumorMutationalLoadWithinRange(input.integer1(), input.integer2());
        };
    }

    @NotNull
    private FunctionCreator hasSpecificHLATypeCreator() {
        return function -> {
            String hlaAlleleToFind = functionInputResolver().createOneHlaAlleleInput(function);
            return new HasSpecificHLAType(hlaAlleleToFind);
        };
    }

    @NotNull
    private FunctionCreator geneIsOverexpressedCreator() {
        return function -> new GeneIsOverexpressed();
    }

    @NotNull
    private FunctionCreator geneIsNotExpressedCreator() {
        return function -> new GeneIsNotExpressed();
    }

    @NotNull
    private FunctionCreator geneIsExpressedByIHCCreator() {
        return function -> {
            String gene = functionInputResolver().createOneStringInput(function);
            return new GeneIsExpressedByIHC(gene);
        };
    }

    @NotNull
    private FunctionCreator geneHasExactExpressionByIHCCreator() {
        return function -> {
            OneIntegerOneString input = functionInputResolver().createOneStringOneIntegerInput(function);
            return new GeneHasExactExpressionByIHC(input.string(), input.integer());
        };
    }

    @NotNull
    private FunctionCreator geneHasSufficientExpressionByIHCCreator() {
        return function -> {
            OneIntegerOneString input = functionInputResolver().createOneStringOneIntegerInput(function);
            return new GeneHasSufficientExpressionByIHC(input.string(), input.integer());
        };
    }

    @NotNull
    private FunctionCreator hasSufficientPDL1ByCPSByIHCCreator() {
        return function -> {
            int minPDL1 = functionInputResolver().createOneIntegerInput(function);
            return new HasSufficientPDL1ByIHC("CPS", minPDL1);
        };
    }

    @NotNull
    private FunctionCreator hasLimitedPDL1ByCPSByIHCCreator() {
        return function -> {
            int maxPDL1 = functionInputResolver().createOneIntegerInput(function);
            return new HasLimitedPDL1ByIHC("CPS", maxPDL1);
        };
    }

    @NotNull
    private FunctionCreator hasLimitedPDL1ByTPSByIHCCreator() {
        return function -> {
            double maxPDL1Percentage = functionInputResolver().createOneDoubleInput(function);
            return new HasLimitedPDL1ByIHC("TPS", maxPDL1Percentage);
        };
    }

    @NotNull
    private FunctionCreator hasAvailablePDL1StatusCreator() {
        return function -> new HasAvailablePDL1Status();
    }

    @NotNull
    private FunctionCreator hasPSMAPositivePETScanCreator() {
        return function -> new HasPSMAPositivePETScan();
    }

    @NotNull
    private FunctionCreator molecularResultsAreGenerallyAvailableCreator() {
        return function -> new MolecularResultsAreGenerallyAvailable();
    }

    @NotNull
    private FunctionCreator molecularResultsAreAvailableForGeneCreator() {
        return function -> {
            String gene = functionInputResolver().createOneStringInput(function);
            return new MolecularResultsAreAvailableForGene(gene);
        };
    }

    @NotNull
    private FunctionCreator molecularResultsAreAvailableForPromoterOfGeneCreator() {
        return function -> {
            String gene = functionInputResolver().createOneStringInput(function);
            return new MolecularResultsAreAvailableForPromoterOfGene(gene);
        };
    }
}
