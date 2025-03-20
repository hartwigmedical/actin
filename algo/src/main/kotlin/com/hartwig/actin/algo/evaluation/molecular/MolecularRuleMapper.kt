package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.evaluation.composite.Or
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.doid.DoidModel

private val EGFR_PACC_VARIANTS = setOf(
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

class MolecularRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.DRIVER_EVENT_IN_ANY_GENES_X_WITH_APPROVED_THERAPY_AVAILABLE to
                    hasMolecularEventInSomeGenesWithApprovedTherapyAvailableCreator(),
            EligibilityRule.HAS_MOLECULAR_EVENT_WITH_SOC_TARGETED_THERAPY_AVAILABLE_IN_NSCLC to
                    { HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(null, emptySet(), maxMolecularTestAge()) },
            EligibilityRule.HAS_MOLECULAR_EVENT_WITH_SOC_TARGETED_THERAPY_AVAILABLE_IN_NSCLC_EXCLUDING_ANY_GENE_X to
                    hasMolecularEventExcludingSomeGeneWithSocTargetedTherapyForNSCLCAvailableCreator(),
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
            EligibilityRule.MSI_SIGNATURE to { IsMicrosatelliteUnstable(maxMolecularTestAge()) },
            EligibilityRule.HRD_SIGNATURE to { IsHomologousRecombinationDeficient(maxMolecularTestAge()) },
            EligibilityRule.HRD_SIGNATURE_WITHOUT_MUTATION_OR_WITH_VUS_MUTATION_IN_GENES_X to isHomologousRecombinationDeficientWithoutMutationOrWithVUSMutationInGenesXCreator(),
            EligibilityRule.HRD_SIGNATURE_WITHOUT_MUTATION_IN_GENES_X to isHomologousRecombinationDeficientWithoutMutationInGenesXCreator(),
            EligibilityRule.TMB_OF_AT_LEAST_X to hasSufficientTumorMutationalBurdenCreator(),
            EligibilityRule.TML_OF_AT_LEAST_X to hasSufficientTumorMutationalLoadCreator(),
            EligibilityRule.TML_BETWEEN_X_AND_Y to hasCertainTumorMutationalLoadCreator(),
            EligibilityRule.HAS_HLA_TYPE_X to hasSpecificHLATypeCreator(),
            EligibilityRule.HAS_HLA_GROUP_X to hasSpecificHLAGroupCreator(),
            EligibilityRule.HAS_UGT1A1_HAPLOTYPE_X to hasUGT1A1HaplotypeCreator(),
            EligibilityRule.HAS_HOMOZYGOUS_DPYD_DEFICIENCY to { HasHomozygousDPYDDeficiency(maxMolecularTestAge()) },
            EligibilityRule.HAS_HETEROZYGOUS_DPYD_DEFICIENCY to { HasHeterozygousDPYDDeficiency(maxMolecularTestAge()) },
            EligibilityRule.HAS_KNOWN_HPV_STATUS to { HasKnownHPVStatus() },
            EligibilityRule.OVEREXPRESSION_OF_ANY_GENE_X to anyGeneFromSetIsOverExpressedCreator(),
            EligibilityRule.NON_EXPRESSION_OF_ANY_GENE_X to anyGeneFromSetIsNotExpressedCreator(),
            EligibilityRule.SPECIFIC_MRNA_EXPRESSION_REQUIREMENTS_MET_FOR_GENES_X to { GenesMeetSpecificMRNAExpressionRequirements() },
            EligibilityRule.LOSS_OF_PROTEIN_X_BY_IHC to proteinIsLostByIHCCreator(),
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC to proteinIsExpressedByIHCCreator(),
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_EXACTLY_Y to proteinHasExactExpressionByIHCCreator(),
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_AT_LEAST_Y to proteinHasSufficientExpressionByIHCCreator(),
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_AT_MOST_Y to proteinHasLimitedExpressionByIHCCreator(),
            EligibilityRule.PROTEIN_X_IS_WILD_TYPE_BY_IHC to proteinIsWildTypeByIHCCreator(),
            EligibilityRule.HER2_STATUS_IS_POSITIVE to hasPositiveHER2ExpressionByIHCCreator(),
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
            EligibilityRule.MOLECULAR_TEST_RESULT_IS_KNOWN_FOR_GENE_X to molecularResultsAreKnownForGeneCreator(),
            EligibilityRule.MOLECULAR_TEST_RESULT_IS_KNOWN_FOR_PROMOTER_OF_GENE_X to molecularResultsAreKnownForPromoterOfGeneCreator(),
            EligibilityRule.MMR_STATUS_IS_AVAILABLE to { MmrStatusIsAvailable(maxMolecularTestAge()) },
            EligibilityRule.HAS_KNOWN_NSCLC_DRIVER_GENE_STATUSES to { NsclcDriverGeneStatusesAreAvailable() },
            EligibilityRule.HAS_EGFR_PACC_MUTATION to hasEgfrPaccMutationCreator(),
            EligibilityRule.HAS_CODELETION_OF_CHROMOSOME_ARMS_X_AND_Y to hasCoDeletionOfChromosomeArmsCreator()
        )
    }

    private fun hasMolecularEventInSomeGenesWithApprovedTherapyAvailableCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createManyGenesInput(function)
            AnyGeneHasDriverEventWithApprovedTherapy(input.geneNames, doidModel(), EvaluationFunctionFactory.create(resources), maxMolecularTestAge())
        }
    }

    private fun hasMolecularEventExcludingSomeGeneWithSocTargetedTherapyForNSCLCAvailableCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genes = functionInputResolver().createManyGenesInput(function)
            HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(null, genes.geneNames, maxMolecularTestAge())
        }
    }

    private fun geneIsActivatedOrAmplifiedCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val gene = functionInputResolver().createOneGeneInput(function).geneName
            Or(
                listOf(
                    GeneHasActivatingMutation(gene, codonsToIgnore = null, maxMolecularTestAge()),
                    GeneIsAmplified(gene, null, maxMolecularTestAge())
                )
            )
        }
    }

    private fun geneIsInactivatedCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            GeneIsInactivated(functionInputResolver().createOneGeneInput(function).geneName, maxMolecularTestAge())
        }
    }

    private fun anyGeneHasActivatingMutationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genes = functionInputResolver().createManyGenesInput(function)
            Or(genes.geneNames.map { GeneHasActivatingMutation(it, codonsToIgnore = null, maxMolecularTestAge()) })
        }
    }

    private fun geneHasActivatingMutationIgnoringSomeCodonsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneGeneManyCodonsInput(function)
            GeneHasActivatingMutation(input.geneName, codonsToIgnore = input.codons, maxMolecularTestAge())
        }
    }

    private fun geneHasVariantWithAnyProteinImpactsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneGeneManyProteinImpactsInput(function)
            GeneHasVariantWithProteinImpact(input.geneName, input.proteinImpacts, maxMolecularTestAge())
        }
    }

    private fun geneHasVariantInAnyCodonsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneGeneManyCodonsInput(function)
            GeneHasVariantInCodon(input.geneName, input.codons, maxMolecularTestAge())
        }
    }

    private fun geneHasVariantInExonCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (gene, exon) = functionInputResolver().createOneGeneOneIntegerInput(function)
            GeneHasVariantInExonRangeOfType(gene, exon, exon, null, maxMolecularTestAge())
        }
    }

    private fun geneHasVariantInExonRangeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (gene, minExon, maxExon) = functionInputResolver().createOneGeneTwoIntegersInput(function)
            GeneHasVariantInExonRangeOfType(gene, minExon, maxExon, null, maxMolecularTestAge())
        }
    }

    private fun geneHasVariantInExonOfTypeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (gene, exon, variantType) = functionInputResolver().createOneGeneOneIntegerOneVariantTypeInput(function)
            GeneHasVariantInExonRangeOfType(gene, exon, exon, variantType, maxMolecularTestAge())
        }
    }

    private fun geneHasUTR3LossCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            GeneHasUTR3Loss(functionInputResolver().createOneGeneInput(function).geneName, maxMolecularTestAge())
        }
    }

    private fun geneIsAmplifiedCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            GeneIsAmplified(functionInputResolver().createOneGeneInput(function).geneName, null, maxMolecularTestAge())
        }
    }

    private fun geneIsAmplifiedMinCopiesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneGeneOneIntegerInput(function)
            GeneIsAmplified(input.geneName, input.integer, maxMolecularTestAge())
        }
    }

    private fun hasFusionInGeneCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasFusionInGene(functionInputResolver().createOneGeneInput(function).geneName, maxMolecularTestAge())
        }
    }

    private fun geneIsWildTypeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            GeneIsWildType(functionInputResolver().createOneGeneInput(function).geneName, maxMolecularTestAge())
        }
    }

    private fun geneHasSpecificExonSkippingCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneGeneOneIntegerInput(function)
            GeneHasSpecificExonSkipping(input.geneName, input.integer, maxMolecularTestAge())
        }
    }

    private fun hasSufficientTumorMutationalBurdenCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minTumorMutationalBurden = functionInputResolver().createOneDoubleInput(function)
            HasSufficientTumorMutationalBurden(minTumorMutationalBurden, maxMolecularTestAge())
        }
    }

    private fun hasSufficientTumorMutationalLoadCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minTumorMutationalLoad = functionInputResolver().createOneIntegerInput(function)
            HasTumorMutationalLoadWithinRange(minTumorMutationalLoad, null, maxMolecularTestAge())
        }
    }

    private fun hasCertainTumorMutationalLoadCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createTwoIntegersInput(function)
            HasTumorMutationalLoadWithinRange(input.integer1, input.integer2, maxMolecularTestAge())
        }
    }

    private fun hasSpecificHLAGroupCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val hlaGroupToFind = functionInputResolver().createOneHlaGroupInput(function)
            HasSpecificHLAType(hlaGroupToFind.group, matchOnHlaGroup = true)
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
            HasUGT1A1Haplotype(haplotypeToFind.haplotype, maxMolecularTestAge())
        }
    }

    private fun anyGeneFromSetIsOverExpressedCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val geneSet = functionInputResolver().createManyGenesInput(function).geneNames
            AnyGeneFromSetIsOverexpressed(maxMolecularTestAge(), geneSet)
        }
    }

    private fun anyGeneFromSetIsNotExpressedCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val geneSet = functionInputResolver().createManyGenesInput(function).geneNames
            AnyGeneFromSetIsNotExpressed(maxMolecularTestAge(), geneSet)
        }
    }

    private fun proteinIsLostByIHCCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            ProteinIsLostByIHC(functionInputResolver().createOneStringInput(function))
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

    private fun hasPositiveHER2ExpressionByIHCCreator(): FunctionCreator {
        return { HasPositiveHER2ExpressionByIHC(maxMolecularTestAge()) }
    }

    private fun proteinHasLimitedExpressionByIHCCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (expressionLevel, protein) = functionInputResolver().createOneStringOneIntegerInput(function)
            ProteinHasLimitedExpressionByIHC(protein, expressionLevel)
        }
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

    private fun molecularResultsAreKnownForGeneCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            MolecularResultsAreKnownForGene(functionInputResolver().createOneGeneInput(function).geneName)
        }
    }

    private fun molecularResultsAreKnownForPromoterOfGeneCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            MolecularResultsAreKnownForPromoterOfGene(functionInputResolver().createOneGeneInput(function).geneName)
        }
    }

    private fun hasEgfrPaccMutationCreator(): FunctionCreator {
        return { GeneHasVariantWithProteinImpact("EGFR", EGFR_PACC_VARIANTS, maxMolecularTestAge()) }
    }

    private fun hasCoDeletionOfChromosomeArmsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (chromosome1, chromosome2) = functionInputResolver().createTwoStringsInput(function)
            HasCodeletionOfChromosomeArms(chromosome1, chromosome2)
        }
    }

    private fun isHomologousRecombinationDeficientWithoutMutationOrWithVUSMutationInGenesXCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genesToFind = functionInputResolver().createManyGenesInput(function)
            IsHomologousRecombinationDeficientWithoutMutationOrWithVUSMutationInGenesX(genesToFind.geneNames, maxMolecularTestAge())
        }
    }

    private fun isHomologousRecombinationDeficientWithoutMutationInGenesXCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genesToFind = functionInputResolver().createManyGenesInput(function)
            IsHomologousRecombinationDeficientWithoutMutationInGenesX(genesToFind.geneNames, maxMolecularTestAge())
        }
    }
}