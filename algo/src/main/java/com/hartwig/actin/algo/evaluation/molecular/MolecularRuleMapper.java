package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.RuleMapper;
import com.hartwig.actin.algo.evaluation.RuleMappingResources;
import com.hartwig.actin.algo.evaluation.composite.Or;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.single.OneGene;
import com.hartwig.actin.treatment.input.single.OneGeneManyCodons;
import com.hartwig.actin.treatment.input.single.OneGeneManyProteinImpacts;
import com.hartwig.actin.treatment.input.single.OneGeneOneInteger;
import com.hartwig.actin.treatment.input.single.OneGeneOneIntegerOneVariantType;
import com.hartwig.actin.treatment.input.single.OneGeneTwoIntegers;
import com.hartwig.actin.treatment.input.single.OneHlaAllele;
import com.hartwig.actin.treatment.input.single.OneIntegerOneString;
import com.hartwig.actin.treatment.input.single.TwoIntegers;

import org.jetbrains.annotations.NotNull;

public class MolecularRuleMapper extends RuleMapper {

    public MolecularRuleMapper(@NotNull final RuleMappingResources resources) {
        super(resources);
    }

    @NotNull
    @Override
    public Map<EligibilityRule, FunctionCreator> createMappings() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.DRIVER_EVENT_IN_ANY_GENES_X_WITH_APPROVED_THERAPY_AVAILABLE,
                anyGeneHasDriverEventWithApprovedTherapyCreator());
        map.put(EligibilityRule.ACTIVATION_OR_AMPLIFICATION_OF_GENE_X, geneIsActivatedOrAmplifiedCreator());
        map.put(EligibilityRule.INACTIVATION_OF_GENE_X, geneIsInactivatedCreator());
        map.put(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X, geneHasActivatingMutationCreator());
        map.put(EligibilityRule.MUTATION_IN_GENE_X_OF_ANY_PROTEIN_IMPACTS_Y, geneHasVariantWithAnyProteinImpactsCreator());
        map.put(EligibilityRule.MUTATION_IN_GENE_X_IN_ANY_CODONS_Y, geneHasVariantInAnyCodonsCreator());
        map.put(EligibilityRule.MUTATION_IN_GENE_X_IN_EXON_Y, geneHasVariantInExonCreator());
        map.put(EligibilityRule.MUTATION_IN_GENE_X_IN_EXON_Y_TO_EXON_Z, geneHasVariantInExonRangeCreator());
        map.put(EligibilityRule.MUTATION_IN_GENE_X_IN_EXON_Y_OF_TYPE_Z, geneHasVariantInExonOfTypeCreator());
        map.put(EligibilityRule.UTR_3_LOSS_IN_GENE_X, geneHasUTR3LossCreator());
        map.put(EligibilityRule.AMPLIFICATION_OF_GENE_X, geneIsAmplifiedCreator());
        map.put(EligibilityRule.AMPLIFICATION_OF_GENE_X_OF_AT_LEAST_Y_COPIES, geneIsAmplifiedMinCopiesCreator());
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
        map.put(EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC, proteinIsExpressedByIHCCreator());
        map.put(EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_EXACTLY_Y, proteinHasExactExpressionByIHCCreator());
        map.put(EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_AT_LEAST_Y, proteinHasSufficientExpressionByIHCCreator());
        map.put(EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_AT_MOST_Y, proteinHasLimitedExpressionByIHCCreator());
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
    private FunctionCreator anyGeneHasDriverEventWithApprovedTherapyCreator() {
        return function -> new AnyGeneHasDriverEventWithApprovedTherapy();
    }

    @NotNull
    private FunctionCreator geneIsActivatedOrAmplifiedCreator() {
        return function -> {
            OneGene gene = functionInputResolver().createOneGeneInput(function);
            return new Or(Lists.newArrayList(new GeneHasActivatingMutation(gene.geneName()), new GeneIsAmplified(gene.geneName())));
        };
    }

    @NotNull
    private FunctionCreator geneIsInactivatedCreator() {
        return function -> {
            OneGene gene = functionInputResolver().createOneGeneInput(function);
            return new GeneIsInactivated(gene.geneName());
        };
    }

    @NotNull
    private FunctionCreator geneHasActivatingMutationCreator() {
        return function -> {
            OneGene gene = functionInputResolver().createOneGeneInput(function);
            return new GeneHasActivatingMutation(gene.geneName());
        };
    }

    @NotNull
    private FunctionCreator geneHasVariantWithAnyProteinImpactsCreator() {
        return function -> {
            OneGeneManyProteinImpacts input = functionInputResolver().createOneGeneManyProteinImpactsInput(function);
            return new GeneHasVariantWithProteinImpact(input.geneName(), input.proteinImpacts());
        };
    }

    @NotNull
    private FunctionCreator geneHasVariantInAnyCodonsCreator() {
        return function -> {
            OneGeneManyCodons input = functionInputResolver().createOneGeneManyCodonsInput(function);
            return new GeneHasVariantInCodon(input.geneName(), input.codons());
        };
    }

    @NotNull
    private FunctionCreator geneHasVariantInExonCreator() {
        return function -> {
            OneGeneOneInteger input = functionInputResolver().createOneGeneOneIntegerInput(function);
            return new GeneHasVariantInExonRangeOfType(input.geneName(), input.integer(), input.integer(), null);
        };
    }

    @NotNull
    private FunctionCreator geneHasVariantInExonRangeCreator() {
        return function -> {
            OneGeneTwoIntegers input = functionInputResolver().createOneGeneTwoIntegersInput(function);
            return new GeneHasVariantInExonRangeOfType(input.geneName(), input.integer1(), input.integer2(), null);
        };
    }

    @NotNull
    private FunctionCreator geneHasVariantInExonOfTypeCreator() {
        return function -> {
            OneGeneOneIntegerOneVariantType input = functionInputResolver().createOneGeneOneIntegerOneVariantTypeInput(function);
            return new GeneHasVariantInExonRangeOfType(input.geneName(), input.integer(), input.integer(), input.variantType());
        };
    }

    @NotNull
    private FunctionCreator geneHasUTR3LossCreator() {
        return function -> {
            OneGene gene = functionInputResolver().createOneGeneInput(function);
            return new GeneHasUTR3Loss(gene.geneName());
        };
    }

    @NotNull
    private FunctionCreator geneIsAmplifiedCreator() {
        return function -> {
            OneGene gene = functionInputResolver().createOneGeneInput(function);
            return new GeneIsAmplified(gene.geneName());
        };
    }

    @NotNull
    private FunctionCreator geneIsAmplifiedMinCopiesCreator() {
        return function -> {
            OneGeneOneDouble input = functionInputResolver().createOneGeneOneDoubleInput(function);
            return new GeneIsAmplifiedMinCopies(input.geneName(), input.double());
        };
    }

    @NotNull
    private FunctionCreator hasFusionInGeneCreator() {
        return function -> {
            OneGene gene = functionInputResolver().createOneGeneInput(function);
            return new HasFusionInGene(gene.geneName());
        };
    }

    @NotNull
    private FunctionCreator geneIsWildTypeCreator() {
        return function -> {
            OneGene gene = functionInputResolver().createOneGeneInput(function);
            return new GeneIsWildType(gene.geneName());
        };
    }

    @NotNull
    private FunctionCreator geneHasSpecificExonSkippingCreator() {
        return function -> {
            OneGeneOneInteger input = functionInputResolver().createOneGeneOneIntegerInput(function);
            return new GeneHasSpecificExonSkipping(input.geneName(), input.integer());
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
            OneHlaAllele hlaAlleleToFind = functionInputResolver().createOneHlaAlleleInput(function);
            return new HasSpecificHLAType(hlaAlleleToFind.allele());
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
    private FunctionCreator proteinIsExpressedByIHCCreator() {
        return function -> {
            String gene = functionInputResolver().createOneStringInput(function);
            return new ProteinIsExpressedByIHC(gene);
        };
    }

    @NotNull
    private FunctionCreator proteinHasExactExpressionByIHCCreator() {
        return function -> {
            OneIntegerOneString input = functionInputResolver().createOneStringOneIntegerInput(function);
            return new ProteinHasExactExpressionByIHC(input.string(), input.integer());
        };
    }

    @NotNull
    private FunctionCreator proteinHasSufficientExpressionByIHCCreator() {
        return function -> {
            OneIntegerOneString input = functionInputResolver().createOneStringOneIntegerInput(function);
            return new ProteinHasExactExpressionByIHC(input.string(), input.integer());
        };
    }

    @NotNull
    private FunctionCreator proteinHasLimitedExpressionByIHCCreator() {
        return function -> new ProteinHasLimitedExpressionByIHCCreator();
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
            OneGene gene = functionInputResolver().createOneGeneInput(function);
            return new MolecularResultsAreAvailableForGene(gene.geneName());
        };
    }

    @NotNull
    private FunctionCreator molecularResultsAreAvailableForPromoterOfGeneCreator() {
        return function -> {
            OneGene gene = functionInputResolver().createOneGeneInput(function);
            return new MolecularResultsAreAvailableForPromoterOfGene(gene.geneName());
        };
    }
}
