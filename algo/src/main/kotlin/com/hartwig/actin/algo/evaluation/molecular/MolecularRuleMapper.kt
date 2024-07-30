package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.evaluation.composite.Or
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule

class MolecularRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.DRIVER_EVENT_IN_ANY_GENES_X_WITH_APPROVED_THERAPY_AVAILABLE to
                    { AnyGeneHasDriverEventWithApprovedTherapy() },
            EligibilityRule.HAS_MOLECULAR_EVENT_WITH_SOC_TARGETED_THERAPY_AVAILABLE_IN_NSCLC to
                    { HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(emptySet()) },
            EligibilityRule.HAS_MOLECULAR_EVENT_WITH_SOC_TARGETED_THERAPY_AVAILABLE_IN_NSCLC_EXCLUDING_ANY_GENE_X to
                    hasMolecularEventExcludingSomeGeneWithSocTargetedTherapyForNSCLCAvailableCreator(),
            EligibilityRule.ACTIVATION_OR_AMPLIFICATION_OF_GENE_X to geneIsActivatedOrAmplifiedCreator(),
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
            EligibilityRule.MSI_SIGNATURE to { IsMicrosatelliteUnstable() },
            EligibilityRule.HRD_SIGNATURE to { IsHomologousRepairDeficient() },
            EligibilityRule.HRD_SIGNATURE_WITHOUT_MUTATION_OR_WITH_VUS_MUTATION_IN_GENES_X to isHomologousRepairDeficientWithoutMutationOrWithVUSMutationInGenesXCreator(),
            EligibilityRule.HRD_SIGNATURE_WITHOUT_MUTATION_IN_GENES_X to isHomologousRepairDeficientWithoutMutationInGenesXCreator(),
            EligibilityRule.TMB_OF_AT_LEAST_X to hasSufficientTumorMutationalBurdenCreator(),
            EligibilityRule.TML_OF_AT_LEAST_X to hasSufficientTumorMutationalLoadCreator(),
            EligibilityRule.TML_BETWEEN_X_AND_Y to hasCertainTumorMutationalLoadCreator(),
            EligibilityRule.HAS_HLA_TYPE_X to hasSpecificHLATypeCreator(),
            EligibilityRule.HAS_UGT1A1_HAPLOTYPE_X to hasUGT1A1HaplotypeCreator(),
            EligibilityRule.HAS_HOMOZYGOUS_DPYD_DEFICIENCY to { HasHomozygousDPYDDeficiency() },
            EligibilityRule.HAS_HETEROZYGOUS_DPYD_DEFICIENCY to { HasHeterozygousDPYDDeficiency() },
            EligibilityRule.HAS_KNOWN_HPV_STATUS to { HasKnownHPVStatus() },
            EligibilityRule.OVEREXPRESSION_OF_GENE_X to { GeneIsOverexpressed() },
            EligibilityRule.NON_EXPRESSION_OF_GENE_X to { GeneIsNotExpressed() },
            EligibilityRule.SPECIFIC_MRNA_EXPRESSION_REQUIREMENTS_MET_FOR_GENES_X to { GenesMeetSpecificMRNAExpressionRequirements() },
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC to proteinIsExpressedByIHCCreator(),
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_EXACTLY_Y to proteinHasExactExpressionByIHCCreator(),
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_AT_LEAST_Y to proteinHasSufficientExpressionByIHCCreator(),
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_AT_MOST_Y to proteinHasLimitedExpressionByIHCCreator(),
            EligibilityRule.PROTEIN_X_IS_WILD_TYPE_BY_IHC to proteinIsWildTypeByIHCCreator(),
            EligibilityRule.PD_L1_SCORE_OF_AT_LEAST_X to hasSufficientPDL1ByMeasureByIHCCreator(),
            EligibilityRule.PD_L1_SCORE_OF_AT_MOST_X to hasLimitedPDL1ByMeasureByIHCCreator(),
            EligibilityRule.PD_L1_SCORE_CPS_OF_AT_LEAST_X to hasSufficientPDL1ByMeasureByIHCCreator("CPS"),
            EligibilityRule.PD_L1_SCORE_CPS_OF_AT_MOST_X to hasLimitedPDL1ByMeasureByIHCCreator("CPS"),
            EligibilityRule.PD_L1_SCORE_TPS_OF_AT_LEAST_X to hasSufficientPDL1ByDoubleMeasureByIHCCreator("TPS", doidModel()),
            EligibilityRule.PD_L1_SCORE_TPS_OF_AT_MOST_X to hasLimitedPDL1ByDoubleMeasureByIHCCreator("TPS", doidModel()),
            EligibilityRule.PD_L1_SCORE_TAP_OF_AT_LEAST_X to hasSufficientPDL1ByDoubleMeasureByIHCCreator("TAP"),
            EligibilityRule.PD_L1_SCORE_TAP_OF_AT_MOST_X to hasLimitedPDL1ByDoubleMeasureByIHCCreator("TAP"),
            EligibilityRule.PD_L1_SCORE_IC_OF_AT_LEAST_X to hasSufficientPDL1ByDoubleMeasureByIHCCreator("IC"),
            EligibilityRule.PD_L1_SCORE_TC_OF_AT_LEAST_X to hasSufficientPDL1ByDoubleMeasureByIHCCreator("TC"),
            EligibilityRule.PD_L1_STATUS_MUST_BE_AVAILABLE to { HasAvailablePDL1Status() },
            EligibilityRule.HAS_PSMA_POSITIVE_PET_SCAN to { HasPSMAPositivePETScan() },
            EligibilityRule.MOLECULAR_RESULTS_MUST_BE_AVAILABLE to { MolecularResultsAreGenerallyAvailable() },
            EligibilityRule.MOLECULAR_TEST_MUST_HAVE_BEEN_DONE_FOR_GENE_X to molecularResultsAreAvailableForGeneCreator(),
            EligibilityRule.MOLECULAR_TEST_MUST_HAVE_BEEN_DONE_FOR_PROMOTER_OF_GENE_X to molecularResultsAreAvailableForPromoterOfGeneCreator(),
            EligibilityRule.MMR_STATUS_IS_AVAILABLE to { MmrStatusIsAvailable() },
            EligibilityRule.HAS_KNOWN_NSCLC_DRIVER_GENE_STATUSES to { NsclcDriverGeneStatusesAreAvailable() },
            EligibilityRule.HAS_EGFR_PACC_MUTATION to hasEgfrPaccMutationCreator(),
            EligibilityRule.HAS_CODELETION_OF_CHROMOSOME_ARMS_X_AND_Y to hasCoDeletionOfChromosomeArmsCreator()
        )
    }

    private fun hasMolecularEventExcludingSomeGeneWithSocTargetedTherapyForNSCLCAvailableCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genes = functionInputResolver().createManyGenesInput(function)
            HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(genes.geneNames.toSet())
        }
    }

    private fun geneIsActivatedOrAmplifiedCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val gene = functionInputResolver().createOneGeneInput(function).geneName
            Or(listOf(GeneHasActivatingMutation(gene, codonsToIgnore = null), GeneIsAmplified(gene, null)))
        }
    }

    private fun geneIsInactivatedCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            GeneIsInactivated(functionInputResolver().createOneGeneInput(function).geneName)
        }
    }

    private fun anyGeneHasActivatingMutationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genes = functionInputResolver().createManyGenesInput(function)
            Or(genes.geneNames.map { GeneHasActivatingMutation(it, codonsToIgnore = null) })
        }
    }

    private fun geneHasActivatingMutationIgnoringSomeCodonsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneGeneManyCodonsInput(function)
            GeneHasActivatingMutation(input.geneName, codonsToIgnore = input.codons)
        }
    }

    private fun geneHasVariantWithAnyProteinImpactsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneGeneManyProteinImpactsInput(function)
            GeneHasVariantWithProteinImpact(input.geneName, input.proteinImpacts)
        }
    }

    private fun geneHasVariantInAnyCodonsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneGeneManyCodonsInput(function)
            GeneHasVariantInCodon(input.geneName, input.codons)
        }
    }

    private fun geneHasVariantInExonCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (gene, exon) = functionInputResolver().createOneGeneOneIntegerInput(function)
            GeneHasVariantInExonRangeOfType(gene, exon, exon, null)
        }
    }

    private fun geneHasVariantInExonRangeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (gene, minExon, maxExon) = functionInputResolver().createOneGeneTwoIntegersInput(function)
            GeneHasVariantInExonRangeOfType(gene, minExon, maxExon, null)
        }
    }

    private fun geneHasVariantInExonOfTypeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (gene, exon, variantType) = functionInputResolver().createOneGeneOneIntegerOneVariantTypeInput(function)
            GeneHasVariantInExonRangeOfType(gene, exon, exon, variantType)
        }
    }

    private fun geneHasUTR3LossCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            GeneHasUTR3Loss(functionInputResolver().createOneGeneInput(function).geneName)
        }
    }

    private fun geneIsAmplifiedCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            GeneIsAmplified(functionInputResolver().createOneGeneInput(function).geneName, null)
        }
    }

    private fun geneIsAmplifiedMinCopiesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneGeneOneIntegerInput(function)
            GeneIsAmplified(input.geneName, input.integer)
        }
    }

    private fun hasFusionInGeneCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasFusionInGene(functionInputResolver().createOneGeneInput(function).geneName)
        }
    }

    private fun geneIsWildTypeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            GeneIsWildType(functionInputResolver().createOneGeneInput(function).geneName)
        }
    }

    private fun geneHasSpecificExonSkippingCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneGeneOneIntegerInput(function)
            GeneHasSpecificExonSkipping(input.geneName, input.integer)
        }
    }

    private fun hasSufficientTumorMutationalBurdenCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minTumorMutationalBurden = functionInputResolver().createOneDoubleInput(function)
            HasSufficientTumorMutationalBurden(minTumorMutationalBurden)
        }
    }

    private fun hasSufficientTumorMutationalLoadCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minTumorMutationalLoad = functionInputResolver().createOneIntegerInput(function)
            HasTumorMutationalLoadWithinRange(minTumorMutationalLoad, null)
        }
    }

    private fun hasCertainTumorMutationalLoadCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createTwoIntegersInput(function)
            HasTumorMutationalLoadWithinRange(input.integer1, input.integer2)
        }
    }

    private fun hasSpecificHLATypeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val hlaAlleleToFind = functionInputResolver().createOneHlaAlleleInput(function)
            HasSpecificHLAType(hlaAlleleToFind.allele)
        }
    }

    private fun hasUGT1A1HaplotypeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val haplotypeToFind = functionInputResolver().createOneHaplotypeInput(function)
            HasUGT1A1Haplotype(haplotypeToFind.haplotype)
        }
    }

    private fun proteinIsExpressedByIHCCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            ProteinIsExpressedByIHC(functionInputResolver().createOneStringInput(function))
        }
    }

    private fun proteinHasExactExpressionByIHCCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (expressionLevel, protein) = functionInputResolver().createOneStringOneIntegerInput(function)
            ProteinHasExactExpressionByIHC(protein, expressionLevel)
        }
    }

    private fun proteinHasSufficientExpressionByIHCCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (expressionLevel, protein) = functionInputResolver().createOneStringOneIntegerInput(function)
            ProteinHasSufficientExpressionByIHC(protein, expressionLevel)
        }
    }

    private fun proteinIsWildTypeByIHCCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            ProteinIsWildTypeByIHC(functionInputResolver().createOneStringInput(function))
        }
    }

    private fun proteinHasLimitedExpressionByIHCCreator(): FunctionCreator {
        return { ProteinHasLimitedExpressionByIHCCreator() }
    }

    private fun hasSufficientPDL1ByMeasureByIHCCreator(measure: String? = null): FunctionCreator {
        return { function: EligibilityFunction ->
            val minPDL1 = functionInputResolver().createOneIntegerInput(function)
            HasSufficientPDL1ByIHC(measure, minPDL1.toDouble())
        }
    }

    private fun hasLimitedPDL1ByMeasureByIHCCreator(measure: String? = null): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxPDL1 = functionInputResolver().createOneIntegerInput(function)
            HasLimitedPDL1ByIHC(measure, maxPDL1.toDouble())
        }
    }

    private fun hasSufficientPDL1ByDoubleMeasureByIHCCreator(measure: String, doidModel: DoidModel? = null): FunctionCreator {
        return { function: EligibilityFunction ->
            val minPDL1 = functionInputResolver().createOneDoubleInput(function)
            HasSufficientPDL1ByIHC(measure, minPDL1, doidModel)
        }
    }

    private fun hasLimitedPDL1ByDoubleMeasureByIHCCreator(measure: String, doidModel: DoidModel? = null): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxPDL1 = functionInputResolver().createOneDoubleInput(function)
            HasLimitedPDL1ByIHC(measure, maxPDL1, doidModel)
        }
    }

    private fun molecularResultsAreAvailableForGeneCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            MolecularResultsAreAvailableForGene(functionInputResolver().createOneGeneInput(function).geneName)
        }
    }

    private fun molecularResultsAreAvailableForPromoterOfGeneCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            MolecularResultsAreAvailableForPromoterOfGene(functionInputResolver().createOneGeneInput(function).geneName)
        }
    }

    private fun hasEgfrPaccMutationCreator(): FunctionCreator {
        return { GeneHasVariantWithProteinImpact("EGFR", EGFR_PACC_VARIANT_LIST) }
    }

    private fun hasCoDeletionOfChromosomeArmsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (chromosome1, chromosome2) = functionInputResolver().createTwoStringsInput(function)
            HasCodeletionOfChromosomeArms(chromosome1, chromosome2)
        }
    }

    private fun isHomologousRepairDeficientWithoutMutationOrWithVUSMutationInGenesXCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genesToFind = functionInputResolver().createManyGenesInput(function)
            IsHomologousRepairDeficientWithoutMutationOrWithVUSMutationInGenesX(genesToFind.geneNames.toSet())
        }
    }

    private fun isHomologousRepairDeficientWithoutMutationInGenesXCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genesToFind = functionInputResolver().createManyGenesInput(function)
            IsHomologousRepairDeficientWithoutMutationInGenesX(genesToFind.geneNames.toSet())
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