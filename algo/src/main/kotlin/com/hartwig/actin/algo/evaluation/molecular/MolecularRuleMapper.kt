package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.evaluation.composite.Or
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule

class MolecularRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.DRIVER_EVENT_IN_ANY_GENES_X_WITH_APPROVED_THERAPY_AVAILABLE to anyGeneHasDriverEventWithApprovedTherapyCreator(),
            EligibilityRule.HAS_MOLECULAR_EVENT_WITH_SOC_TARGETED_THERAPY_AVAILABLE_IN_NSCLC to hasMolecularEventWithSocTargetedTherapyForNSCLCAvailableCreator(),
            EligibilityRule.HAS_MOLECULAR_EVENT_WITH_SOC_TARGETED_THERAPY_AVAILABLE_IN_NSCLC_EXCLUDING_ANY_GENE_X to hasMolecularEventExcludingSomeGeneWithSocTargetedTherapyForNSCLCAvailableCreator(),
            EligibilityRule.ACTIVATION_OR_AMPLIFICATION_OF_GENE_X to geneIsActivatedOrAmplifiedCreator(),
            EligibilityRule.INACTIVATION_OF_GENE_X to geneIsInactivatedCreator(),
            EligibilityRule.ACTIVATING_MUTATION_IN_ANY_GENES_X to anyGeneHasActivatingMutationCreator(),
            EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X_EXCLUDING_CODONS_Y to geneHasActivatingMutationIgnoringSomeCodonsCreator(),
            EligibilityRule.MUTATION_IN_GENE_X_OF_ANY_PROTEIN_IMPACTS_Y to geneHasVariantWithAnyProteinImpactsCreator(),
            EligibilityRule.MUTATION_IN_GENE_X_IN_ANY_CODONS_Y to geneHasVariantInAnyCodonsCreator(),
            EligibilityRule.MUTATION_IN_GENE_X_IN_EXON_Y to geneHasVariantInExonCreator(),
            EligibilityRule.MUTATION_IN_GENE_X_IN_EXON_Y_TO_EXON_Z to geneHasVariantInExonRangeCreator(),
            EligibilityRule.MUTATION_IN_GENE_X_IN_EXON_Y_OF_TYPE_Z to geneHasVariantInExonOfTypeCreator(),
            EligibilityRule.UTR_3_LOSS_IN_GENE_X to geneHasUTR3LossCreator(),
            EligibilityRule.AMPLIFICATION_OF_GENE_X to geneIsAmplifiedCreator(),
            EligibilityRule.AMPLIFICATION_OF_GENE_X_OF_AT_LEAST_Y_COPIES to geneIsAmplifiedMinCopiesCreator(),
            EligibilityRule.FUSION_IN_GENE_X to hasFusionInGeneCreator(),
            EligibilityRule.WILDTYPE_OF_GENE_X to geneIsWildTypeCreator(),
            EligibilityRule.EXON_SKIPPING_GENE_X_EXON_Y to geneHasSpecificExonSkippingCreator(),
            EligibilityRule.MSI_SIGNATURE to isMicrosatelliteUnstableCreator,
            EligibilityRule.HRD_SIGNATURE to isHomologousRepairDeficientCreator,
            EligibilityRule.HRD_SIGNATURE_WITHOUT_MUTATION_OR_WITH_VUS_MUTATION_IN_BRCA to isHomologousRepairDeficientWithoutMutationOrWithVUSMutationInBRCA,
            EligibilityRule.TMB_OF_AT_LEAST_X to hasSufficientTumorMutationalBurdenCreator(),
            EligibilityRule.TML_OF_AT_LEAST_X to hasSufficientTumorMutationalLoadCreator(),
            EligibilityRule.TML_BETWEEN_X_AND_Y to hasCertainTumorMutationalLoadCreator(),
            EligibilityRule.HAS_HLA_TYPE_X to hasSpecificHLATypeCreator(),
            EligibilityRule.HAS_UGT1A1_HAPLOTYPE_X to hasUGT1A1HaplotypeCreator(),
            EligibilityRule.HAS_HOMOZYGOUS_DPYD_DEFICIENCY to hasHomozygousDPYDDeficiencyCreator(),
            EligibilityRule.HAS_KNOWN_HPV_STATUS to hasKnownHPVStatusCreator(),
            EligibilityRule.OVEREXPRESSION_OF_GENE_X to geneIsOverexpressedCreator(),
            EligibilityRule.NON_EXPRESSION_OF_GENE_X to geneIsNotExpressedCreator(),
            EligibilityRule.SPECIFIC_MRNA_EXPRESSION_REQUIREMENTS_MET_FOR_GENES_X to genesMeetSpecificMRNAExpressionRequirementsCreator(),
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC to proteinIsExpressedByIHCCreator(),
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_EXACTLY_Y to proteinHasExactExpressionByIHCCreator(),
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_AT_LEAST_Y to proteinHasSufficientExpressionByIHCCreator(),
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_AT_MOST_Y to proteinHasLimitedExpressionByIHCCreator(),
            EligibilityRule.PROTEIN_X_IS_WILD_TYPE_BY_IHC to proteinIsWildTypeByIHCCreator(),
            EligibilityRule.PD_L1_SCORE_OF_AT_LEAST_X to hasSufficientPDL1ByIHCCreator(),
            EligibilityRule.PD_L1_SCORE_OF_AT_MOST_X to hasLimitedPDL1ByIHCCreator(),
            EligibilityRule.PD_L1_SCORE_CPS_OF_AT_LEAST_X to hasSufficientPDL1ByCPSByIHCCreator(),
            EligibilityRule.PD_L1_SCORE_CPS_OF_AT_MOST_X to hasLimitedPDL1ByCPSByIHCCreator(),
            EligibilityRule.PD_L1_SCORE_TPS_OF_AT_MOST_X to hasLimitedPDL1ByTPSByIHCCreator(),
            EligibilityRule.PD_L1_SCORE_TAP_OF_AT_MOST_X to hasLimitedPDL1ByTAPByIHCCreator(),
            EligibilityRule.PD_L1_SCORE_TPS_OF_AT_LEAST_X to hasSufficientPDL1ByTPSByIHCCreator(),
            EligibilityRule.PD_L1_SCORE_IC_OF_AT_LEAST_X to hasSufficientPDL1ByICByIHCCreator(),
            EligibilityRule.PD_L1_SCORE_TC_OF_AT_LEAST_X to hasSufficientPDL1ByTCByIHCCreator(),
            EligibilityRule.PD_L1_SCORE_TAP_OF_AT_LEAST_X to hasSufficientPDL1ByTAPByIHCCreator(),
            EligibilityRule.PD_L1_STATUS_MUST_BE_AVAILABLE to hasAvailablePDL1StatusCreator(),
            EligibilityRule.HAS_PSMA_POSITIVE_PET_SCAN to hasPSMAPositivePETScanCreator(),
            EligibilityRule.MOLECULAR_RESULTS_MUST_BE_AVAILABLE to molecularResultsAreGenerallyAvailableCreator(),
            EligibilityRule.MOLECULAR_TEST_MUST_HAVE_BEEN_DONE_FOR_GENE_X to molecularResultsAreAvailableForGeneCreator(),
            EligibilityRule.MOLECULAR_TEST_MUST_HAVE_BEEN_DONE_FOR_PROMOTER_OF_GENE_X to molecularResultsAreAvailableForPromoterOfGeneCreator(),
            EligibilityRule.MMR_STATUS_MUST_BE_AVAILABLE to mmrStatusIsGenerallyAvailableCreator(),
            EligibilityRule.HAS_KNOWN_NSCLC_DRIVER_GENE_STATUSES to nsclcDriverGeneStatusesAreAvailableCreator(),
            EligibilityRule.HAS_EGFR_PACC_MUTATION to hasEgfrPaccMutationCreator(),
            EligibilityRule.HAS_CODELETION_OF_CHROMOSOME_ARMS_X_AND_Y to hasCoDeletionOfChromosomeArmsCreator()
        )
    }

    private fun anyGeneHasDriverEventWithApprovedTherapyCreator(): FunctionCreator {
        return FunctionCreator { AnyGeneHasDriverEventWithApprovedTherapy() }
    }

    private fun hasMolecularEventWithSocTargetedTherapyForNSCLCAvailableCreator(): FunctionCreator {
        return FunctionCreator { HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(emptySet()) }
    }

    private fun hasMolecularEventExcludingSomeGeneWithSocTargetedTherapyForNSCLCAvailableCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val genes = functionInputResolver().createManyGenesInput(function)
            HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(genes.geneNames.toSet())
        }
    }

    private fun geneIsActivatedOrAmplifiedCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val gene = functionInputResolver().createOneGeneInput(function)
            Or(listOf(GeneHasActivatingMutation(gene.geneName, codonsToIgnore = null), GeneIsAmplified(gene.geneName, null)))
        }
    }

    private fun geneIsInactivatedCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val gene = functionInputResolver().createOneGeneInput(function)
            GeneIsInactivated(gene.geneName)
        }
    }

    private fun anyGeneHasActivatingMutationCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val genes = functionInputResolver().createManyGenesInput(function)
            Or(genes.geneNames.map { GeneHasActivatingMutation(it, codonsToIgnore = null) })
        }
    }

    private fun geneHasActivatingMutationIgnoringSomeCodonsCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneGeneManyCodonsInput(function)
            GeneHasActivatingMutation(input.geneName, codonsToIgnore = input.codons)
        }
    }

    private fun geneHasVariantWithAnyProteinImpactsCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneGeneManyProteinImpactsInput(
                function
            )
            GeneHasVariantWithProteinImpact(input.geneName, input.proteinImpacts)
        }
    }

    private fun geneHasVariantInAnyCodonsCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneGeneManyCodonsInput(function)
            GeneHasVariantInCodon(input.geneName, input.codons)
        }
    }

    private fun geneHasVariantInExonCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneGeneOneIntegerInput(function)
            GeneHasVariantInExonRangeOfType(input.geneName, input.integer, input.integer, null)
        }
    }

    private fun geneHasVariantInExonRangeCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneGeneTwoIntegersInput(function)
            GeneHasVariantInExonRangeOfType(input.geneName, input.integer1, input.integer2, null)
        }
    }

    private fun geneHasVariantInExonOfTypeCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneGeneOneIntegerOneVariantTypeInput(
                function
            )
            GeneHasVariantInExonRangeOfType(input.geneName, input.integer, input.integer, input.variantType)
        }
    }

    private fun geneHasUTR3LossCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val gene = functionInputResolver().createOneGeneInput(function)
            GeneHasUTR3Loss(gene.geneName)
        }
    }

    private fun geneIsAmplifiedCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val gene = functionInputResolver().createOneGeneInput(function)
            GeneIsAmplified(gene.geneName, null)
        }
    }

    private fun geneIsAmplifiedMinCopiesCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneGeneOneIntegerInput(function)
            GeneIsAmplified(input.geneName, input.integer)
        }
    }

    private fun hasFusionInGeneCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val gene = functionInputResolver().createOneGeneInput(function)
            HasFusionInGene(gene.geneName)
        }
    }

    private fun geneIsWildTypeCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val gene = functionInputResolver().createOneGeneInput(function)
            GeneIsWildType(gene.geneName)
        }
    }

    private fun geneHasSpecificExonSkippingCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneGeneOneIntegerInput(function)
            GeneHasSpecificExonSkipping(input.geneName, input.integer)
        }
    }

    private val isMicrosatelliteUnstableCreator: FunctionCreator
        get() = FunctionCreator { IsMicrosatelliteUnstable() }
    private val isHomologousRepairDeficientCreator: FunctionCreator
        get() = FunctionCreator { IsHomologousRepairDeficient() }
    private val isHomologousRepairDeficientWithoutMutationOrWithVUSMutationInBRCA: FunctionCreator
        get() = FunctionCreator { IsHomologousRepairDeficientWithoutMutationOrWithVUSMutationInBRCA() }

    private fun hasSufficientTumorMutationalBurdenCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minTumorMutationalBurden = functionInputResolver().createOneDoubleInput(function)
            HasSufficientTumorMutationalBurden(minTumorMutationalBurden)
        }
    }

    private fun hasSufficientTumorMutationalLoadCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minTumorMutationalLoad = functionInputResolver().createOneIntegerInput(function)
            HasTumorMutationalLoadWithinRange(minTumorMutationalLoad, null)
        }
    }

    private fun hasCertainTumorMutationalLoadCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createTwoIntegersInput(function)
            HasTumorMutationalLoadWithinRange(input.integer1, input.integer2)
        }
    }

    private fun hasSpecificHLATypeCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val hlaAlleleToFind = functionInputResolver().createOneHlaAlleleInput(function)
            HasSpecificHLAType(hlaAlleleToFind.allele)
        }
    }

    private fun hasUGT1A1HaplotypeCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val haplotypeToFind = functionInputResolver().createOneHaplotypeInput(function)
            HasUGT1A1Haplotype(haplotypeToFind.haplotype)
        }
    }

    private fun hasHomozygousDPYDDeficiencyCreator(): FunctionCreator {
        return FunctionCreator { HasHomozygousDPYDDeficiency() }
    }

    private fun hasKnownHPVStatusCreator(): FunctionCreator {
        return FunctionCreator { HasKnownHPVStatus() }
    }

    private fun geneIsOverexpressedCreator(): FunctionCreator {
        return FunctionCreator { GeneIsOverexpressed() }
    }

    private fun geneIsNotExpressedCreator(): FunctionCreator {
        return FunctionCreator { GeneIsNotExpressed() }
    }

    private fun genesMeetSpecificMRNAExpressionRequirementsCreator(): FunctionCreator {
        return FunctionCreator { GenesMeetSpecificMRNAExpressionRequirements() }
    }

    private fun proteinIsExpressedByIHCCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val gene = functionInputResolver().createOneStringInput(function)
            ProteinIsExpressedByIHC(gene)
        }
    }

    private fun proteinHasExactExpressionByIHCCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneStringOneIntegerInput(function)
            ProteinHasExactExpressionByIHC(input.string, input.integer)
        }
    }

    private fun proteinHasSufficientExpressionByIHCCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneStringOneIntegerInput(function)
            ProteinHasExactExpressionByIHC(input.string, input.integer)
        }
    }

    private fun proteinIsWildTypeByIHCCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            ProteinIsWildTypeByIHC(
                functionInputResolver().createOneStringInput(
                    function
                )
            )
        }
    }

    private fun proteinHasLimitedExpressionByIHCCreator(): FunctionCreator {
        return FunctionCreator { ProteinHasLimitedExpressionByIHCCreator() }
    }

    private fun hasSufficientPDL1ByIHCCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minPDL1 = functionInputResolver().createOneIntegerInput(function)
            HasSufficientPDL1ByIHC(null, minPDL1.toDouble())
        }
    }

    private fun hasLimitedPDL1ByIHCCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val maxPDL1 = functionInputResolver().createOneIntegerInput(function)
            HasLimitedPDL1ByIHC(null, maxPDL1.toDouble())
        }
    }

    private fun hasSufficientPDL1ByCPSByIHCCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minPDL1 = functionInputResolver().createOneIntegerInput(function)
            HasSufficientPDL1ByIHC("CPS", minPDL1.toDouble())
        }
    }

    private fun hasLimitedPDL1ByCPSByIHCCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val maxPDL1 = functionInputResolver().createOneIntegerInput(function)
            HasLimitedPDL1ByIHC("CPS", maxPDL1.toDouble())
        }
    }

    private fun hasLimitedPDL1ByTPSByIHCCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val maxPDL1Percentage = functionInputResolver().createOneDoubleInput(function)
            HasLimitedPDL1ByIHC("TPS", maxPDL1Percentage, doidModel())
        }
    }

    private fun hasLimitedPDL1ByTAPByIHCCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val maxPDL1Percentage = functionInputResolver().createOneDoubleInput(function)
            HasLimitedPDL1ByIHC("TAP", maxPDL1Percentage, doidModel())
        }
    }

    private fun hasSufficientPDL1ByTPSByIHCCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minPDL1Percentage = functionInputResolver().createOneDoubleInput(function)
            HasSufficientPDL1ByIHC("TPS", minPDL1Percentage, doidModel())
        }
    }

    private fun hasSufficientPDL1ByICByIHCCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minPDL1Percentage = functionInputResolver().createOneDoubleInput(function)
            HasSufficientPDL1ByIHC("IC", minPDL1Percentage)
        }
    }

    private fun hasSufficientPDL1ByTCByIHCCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minPDL1Percentage = functionInputResolver().createOneDoubleInput(function)
            HasSufficientPDL1ByIHC("TC", minPDL1Percentage)
        }
    }

    private fun hasSufficientPDL1ByTAPByIHCCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minPDL1Percentage = functionInputResolver().createOneDoubleInput(function)
            HasSufficientPDL1ByIHC("TAP", minPDL1Percentage)
        }
    }

    private fun hasAvailablePDL1StatusCreator(): FunctionCreator {
        return FunctionCreator { HasAvailablePDL1Status() }
    }

    private fun hasPSMAPositivePETScanCreator(): FunctionCreator {
        return FunctionCreator { HasPSMAPositivePETScan() }
    }

    private fun molecularResultsAreGenerallyAvailableCreator(): FunctionCreator {
        return FunctionCreator { MolecularResultsAreGenerallyAvailable() }
    }

    private fun molecularResultsAreAvailableForGeneCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val gene = functionInputResolver().createOneGeneInput(function)
            MolecularResultsAreAvailableForGene(gene.geneName)
        }
    }

    private fun molecularResultsAreAvailableForPromoterOfGeneCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val gene = functionInputResolver().createOneGeneInput(function)
            MolecularResultsAreAvailableForPromoterOfGene(gene.geneName)
        }
    }

    private fun mmrStatusIsGenerallyAvailableCreator(): FunctionCreator {
        return FunctionCreator { MmrStatusIsGenerallyAvailable() }
    }

    private fun nsclcDriverGeneStatusesAreAvailableCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            NsclcDriverGeneStatusesAreAvailable()
        }
    }

    private fun hasEgfrPaccMutationCreator(): FunctionCreator {
        return FunctionCreator { GeneHasVariantWithProteinImpact("EGFR", EGFR_PACC_VARIANT_LIST) }
    }

    private fun hasCoDeletionOfChromosomeArmsCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val (chromosome1, chromosome2) = functionInputResolver().createTwoStringsInput(function)
            HasCodeletionOfChromosomeArms(chromosome1, chromosome2)
        }
    }

    private val EGFR_PACC_VARIANT_LIST =
        listOf(
            "G719X",
            "S768I",
            "L747P",
            "L747S",
            "V769L",
            "E709_T710 delinsD",
            "C797S",
            "L792H",
            "G724S",
            "L718X",
            "T854I",
        )
}