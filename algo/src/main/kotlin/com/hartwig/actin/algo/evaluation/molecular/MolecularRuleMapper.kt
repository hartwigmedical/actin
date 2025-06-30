package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.evaluation.composite.Or
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.doid.DoidModel

private val EGFR_PACC_PROTEIN_IMPACTS = setOf(
    "S768I",
    "L747P",
    "L747S",
    "V769L",
    "E709_T710delinsD",
    "C797S",
    "L792H",
    "G724S",
    "T854I",
)
private val EGFR_PACC_CODON_VARIANTS = listOf(
    "L718",
    "G719",
)
private val NSCLC_DRIVER_GENES_WITH_AVAILABLE_SOC_FIRST_LINE =
    setOf("ALK", "EGFR", "NTRK1", "NTRK2", "NTRK3", "RET", "ROS1")
val NSCLC_DRIVER_GENES_WITH_AVAILABLE_SOC_ANY_LINE =
    NSCLC_DRIVER_GENES_WITH_AVAILABLE_SOC_FIRST_LINE + setOf("BRAF", "ERBB2", "KRAS", "MET")

class MolecularRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_MOLECULAR_DRIVER_EVENT_WITH_APPROVED_THERAPY_AVAILABLE to
                    hasMolecularDriverEventInAnyGenesWithApprovedTherapyAvailableCreator(),
            EligibilityRule.HAS_MOLECULAR_DRIVER_EVENT_IN_ANY_GENES_X_WITH_APPROVED_THERAPY_AVAILABLE to
                    hasMolecularDriverEventInSomeGenesWithApprovedTherapyAvailableCreator(),
            EligibilityRule.HAS_MOLECULAR_DRIVER_EVENT_IN_NSCLC to
                    { HasMolecularDriverEventInNsclc(null, emptySet(), maxMolecularTestAge(), false, false) },
            EligibilityRule.HAS_MOLECULAR_DRIVER_EVENT_IN_NSCLC_IN_ANY_GENES_X to
                    hasMolecularDriverEventInNSCLCInSpecificGenesCreator(),
            EligibilityRule.HAS_MOLECULAR_DRIVER_EVENT_IN_NSCLC_IN_AT_LEAST_GENES_X to
                    hasMolecularDriverEventInNSCLCInAtLeastSpecificGenesCreator(),
            EligibilityRule.HAS_MOLECULAR_DRIVER_EVENT_IN_NSCLC_EXCLUDING_GENES_X to
                    hasMolecularDriverEventInNSCLCInExcludingSomeGenesCreator(),
            EligibilityRule.HAS_MOLECULAR_DRIVER_EVENT_IN_NSCLC_WITH_AVAILABLE_SOC_ANY_LINE to
                    hasMolecularEventInNSCLCWithAvailableSocAnyLineCreator(),
            EligibilityRule.HAS_MOLECULAR_DRIVER_EVENT_IN_NSCLC_WITH_AVAILABLE_SOC_ANY_LINE_EXCLUDING_GENES_X to
                    hasMolecularEventInNSCLCWithAvailableSocAnyLineExcludingSomeGenesCreator(),
            EligibilityRule.HAS_MOLECULAR_DRIVER_EVENT_IN_NSCLC_WITH_AVAILABLE_SOC_FIRST_LINE to
                    hasMolecularEventInNSCLCWithAvailableSocFirstLineCreator(),
            EligibilityRule.HAS_MOLECULAR_DRIVER_EVENT_IN_NSCLC_WITH_AVAILABLE_SOC_FIRST_LINE_EXCLUDING_GENES_X to
                    hasMolecularEventInNSCLCWithAvailableSocFirstLineExcludingSomeGenesCreator(),
            EligibilityRule.ACTIVATION_OR_AMPLIFICATION_OF_GENE_X to geneIsActivatedOrAmplifiedCreator(),
            EligibilityRule.INACTIVATION_OF_GENE_X to geneIsInactivatedCreator(),
            EligibilityRule.ACTIVATING_MUTATION_IN_ANY_GENES_X to anyGeneHasActivatingMutationCreator(),
            EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X_EXCLUDING_CODONS_Y to geneHasActivatingMutationIgnoringSomeCodonsCreator(),
            EligibilityRule.ACTIVATING_MUTATION_IN_KINASE_DOMAIN_IN_ANY_GENES_X to anyGeneHasActivatingMutationInKinaseDomainCreator(),
            EligibilityRule.MUTATION_IN_GENE_X_OF_ANY_PROTEIN_IMPACTS_Y to geneHasVariantWithAnyProteinImpactsCreator(),
            EligibilityRule.MUTATION_IN_GENE_X_IN_ANY_CODONS_Y to geneHasVariantInAnyCodonsCreator(),
            EligibilityRule.MUTATION_IN_GENE_X_IN_EXON_Y to geneHasVariantInExonCreator(),
            EligibilityRule.MUTATION_IN_GENE_X_IN_EXON_Y_TO_EXON_Z to geneHasVariantInExonRangeCreator(),
            EligibilityRule.MUTATION_IN_GENE_X_IN_EXON_Y_OF_TYPE_Z to geneHasVariantInExonOfTypeCreator(),
            EligibilityRule.UTR_3_LOSS_IN_GENE_X to geneHasUTR3LossCreator(),
            EligibilityRule.AMPLIFICATION_OF_GENE_X to geneIsAmplifiedCreator(),
            EligibilityRule.AMPLIFICATION_OF_GENE_X_OF_AT_LEAST_Y_COPIES to geneIsAmplifiedMinCopiesCreator(),
            EligibilityRule.COPY_NUMBER_OF_GENE_X_OF_AT_LEAST_Y to geneHasSufficientCopyNumber(),
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
            EligibilityRule.HAS_ANY_HLA_TYPE_X to hasAnyHLATypeCreator(),
            EligibilityRule.HAS_HLA_GROUP_X to hasSpecificHLAGroupCreator(),
            EligibilityRule.HAS_UGT1A1_HAPLOTYPE_X to hasUGT1A1HaplotypeCreator(),
            EligibilityRule.HAS_HOMOZYGOUS_DPYD_DEFICIENCY to { HasHomozygousDPYDDeficiency(maxMolecularTestAge()) },
            EligibilityRule.HAS_HETEROZYGOUS_DPYD_DEFICIENCY to { HasHeterozygousDPYDDeficiency(maxMolecularTestAge()) },
            EligibilityRule.HAS_KNOWN_HPV_STATUS to { HasKnownHPVStatus() },
            EligibilityRule.OVEREXPRESSION_OF_ANY_GENE_X to anyGeneFromSetIsOverExpressedCreator(),
            EligibilityRule.NON_EXPRESSION_OF_ANY_GENE_X to anyGeneFromSetIsNotExpressedCreator(),
            EligibilityRule.SPECIFIC_MRNA_EXPRESSION_REQUIREMENTS_MET_FOR_GENES_X to { GenesMeetSpecificMRNAExpressionRequirements() },
            EligibilityRule.LOSS_OF_PROTEIN_X_BY_IHC to proteinIsLostByIhcCreator(),
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC to proteinIsExpressedByIhcCreator(),
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_EXACTLY_Y to proteinHasExactExpressionByIhcCreator(),
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_AT_LEAST_Y to proteinHasSufficientExpressionByIhcCreator(),
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_AT_MOST_Y to proteinHasLimitedExpressionByIhcCreator(),
            EligibilityRule.PROTEIN_X_IS_WILD_TYPE_BY_IHC to proteinIsWildTypeByIhcCreator(),
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_MUST_BE_AVAILABLE to hasAvailableProteinExpressionCreator(),
            EligibilityRule.HER2_STATUS_IS_POSITIVE to hasPositiveHER2ExpressionByIhcCreator(),
            EligibilityRule.PD_L1_SCORE_OF_AT_LEAST_X to hasSufficientPDL1ByMeasureByIhcCreator(),
            EligibilityRule.PD_L1_SCORE_OF_AT_MOST_X to hasLimitedPDL1ByMeasureByIhcCreator(),
            EligibilityRule.PD_L1_SCORE_CPS_OF_AT_LEAST_X to hasSufficientPDL1ByMeasureByIhcCreator("CPS"),
            EligibilityRule.PD_L1_SCORE_CPS_OF_AT_MOST_X to hasLimitedPDL1ByMeasureByIhcCreator("CPS"),
            EligibilityRule.PD_L1_SCORE_TPS_OF_AT_LEAST_X to hasSufficientPDL1ByDoubleMeasureByIhcCreator("TPS", doidModel()),
            EligibilityRule.PD_L1_SCORE_TPS_OF_AT_MOST_X to hasLimitedPDL1ByDoubleMeasureByIhcCreator("TPS", doidModel()),
            EligibilityRule.PD_L1_SCORE_TAP_OF_AT_LEAST_X to hasSufficientPDL1ByDoubleMeasureByIhcCreator("TAP"),
            EligibilityRule.PD_L1_SCORE_TAP_OF_AT_MOST_X to hasLimitedPDL1ByDoubleMeasureByIhcCreator("TAP"),
            EligibilityRule.PD_L1_SCORE_IC_OF_AT_LEAST_X to hasSufficientPDL1ByDoubleMeasureByIhcCreator("IC"),
            EligibilityRule.PD_L1_SCORE_TC_OF_AT_LEAST_X to hasSufficientPDL1ByDoubleMeasureByIhcCreator("TC"),
            EligibilityRule.PD_L1_STATUS_MUST_BE_AVAILABLE to { HasAvailablePDL1Status() },
            EligibilityRule.HAS_PSMA_POSITIVE_PET_SCAN to { HasPSMAPositivePETScan() },
            EligibilityRule.MOLECULAR_RESULTS_MUST_BE_AVAILABLE to { MolecularResultsAreGenerallyAvailable() },
            EligibilityRule.MOLECULAR_TEST_RESULT_IS_KNOWN_FOR_GENE_X to molecularResultsAreKnownForGeneCreator(),
            EligibilityRule.MOLECULAR_TEST_RESULT_IS_KNOWN_FOR_PROMOTER_OF_GENE_X to molecularResultsAreKnownForPromoterOfGeneCreator(),
            EligibilityRule.MMR_STATUS_IS_AVAILABLE to { MmrStatusIsAvailable(maxMolecularTestAge()) },
            EligibilityRule.HAS_KNOWN_NSCLC_DRIVER_GENE_STATUSES to { NsclcDriverGeneStatusesAreAvailable() },
            EligibilityRule.HAS_EGFR_PACC_MUTATION to hasEgfrPaccMutationCreator(),
            EligibilityRule.HAS_CODELETION_OF_CHROMOSOME_ARMS_X_AND_Y to hasCoDeletionOfChromosomeArmsCreator(),
            EligibilityRule.HAS_PROTEIN_X_POLYMORPHISM_Y to hasProteinPolymorphismCreator()
        )
    }

    private fun hasMolecularDriverEventInAnyGenesWithApprovedTherapyAvailableCreator(): FunctionCreator {
        return {
            AnyGeneHasDriverEventWithApprovedTherapy(
                null,
                doidModel(),
                EvaluationFunctionFactory.create(resources),
                maxMolecularTestAge()
            )
        }
    }

    private fun hasMolecularDriverEventInSomeGenesWithApprovedTherapyAvailableCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createManyGenesInput(function)
            AnyGeneHasDriverEventWithApprovedTherapy(
                input.geneNames,
                doidModel(),
                EvaluationFunctionFactory.create(resources),
                maxMolecularTestAge()
            )
        }
    }

    private fun hasMolecularDriverEventInNSCLCInSpecificGenesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genes = functionInputResolver().createManyGenesInput(function)
            HasMolecularDriverEventInNsclc(genes.geneNames, emptySet(), maxMolecularTestAge(), false, false)
        }
    }

    private fun hasMolecularDriverEventInNSCLCInAtLeastSpecificGenesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genes = functionInputResolver().createManyGenesInput(function)
            HasMolecularDriverEventInNsclc(genes.geneNames, emptySet(), maxMolecularTestAge(), true, false)
        }
    }

    private fun hasMolecularDriverEventInNSCLCInExcludingSomeGenesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genes = functionInputResolver().createManyGenesInput(function)
            HasMolecularDriverEventInNsclc(null, genes.geneNames, maxMolecularTestAge(), false, false)
        }
    }

    private fun hasMolecularEventInNSCLCWithAvailableSocAnyLineCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasMolecularDriverEventInNsclc(
                NSCLC_DRIVER_GENES_WITH_AVAILABLE_SOC_ANY_LINE,
                emptySet(),
                maxMolecularTestAge(),
                false,
                true
            )
        }
    }

    private fun hasMolecularEventInNSCLCWithAvailableSocAnyLineExcludingSomeGenesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genes = setOf(functionInputResolver().createManyGenesInput(function).toString())
            HasMolecularDriverEventInNsclc(
                NSCLC_DRIVER_GENES_WITH_AVAILABLE_SOC_ANY_LINE - genes,
                emptySet(),
                maxMolecularTestAge(),
                false,
                true
            )
        }
    }

    private fun hasMolecularEventInNSCLCWithAvailableSocFirstLineCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasMolecularDriverEventInNsclc(
                NSCLC_DRIVER_GENES_WITH_AVAILABLE_SOC_FIRST_LINE,
                emptySet(),
                maxMolecularTestAge(),
                false,
                true
            )
        }
    }

    private fun hasMolecularEventInNSCLCWithAvailableSocFirstLineExcludingSomeGenesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genes = setOf(functionInputResolver().createManyGenesInput(function).toString())
            HasMolecularDriverEventInNsclc(
                NSCLC_DRIVER_GENES_WITH_AVAILABLE_SOC_FIRST_LINE - genes,
                emptySet(),
                maxMolecularTestAge(),
                false,
                true
            )
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

    private fun anyGeneHasActivatingMutationInKinaseDomainCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genes = functionInputResolver().createManyGenesInput(function)
            Or(genes.geneNames.map { GeneHasActivatingMutation(it, codonsToIgnore = null, maxMolecularTestAge(), inKinaseDomain = true) })
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

    private fun geneHasSufficientCopyNumber(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneGeneOneIntegerInput(function)
            GeneHasSufficientCopyNumber(input.geneName, input.integer, maxMolecularTestAge())
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
            HasAnyHLAType(setOf(hlaGroupToFind.group), matchOnHlaGroup = true)
        }
    }

    private fun hasAnyHLATypeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val hlaAllelesToFind = functionInputResolver().createManyHlaAllelesInput(function)
            HasAnyHLAType(hlaAllelesToFind.alleles)
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
            val genes = functionInputResolver().createManyGenesInput(function).geneNames
            AnyGeneFromSetIsOverexpressed(maxMolecularTestAge(), genes)
        }
    }

    private fun anyGeneFromSetIsNotExpressedCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val geneSet = functionInputResolver().createManyGenesInput(function).geneNames
            AnyGeneFromSetIsNotExpressed(maxMolecularTestAge(), geneSet)
        }
    }

    private fun proteinIsLostByIhcCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            ProteinIsLostByIhc(functionInputResolver().createOneProteinInput(function).proteinName)
        }
    }

    private fun proteinIsExpressedByIhcCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            ProteinIsExpressedByIhc(functionInputResolver().createOneProteinInput(function).proteinName)
        }
    }

    private fun proteinHasExactExpressionByIhcCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (protein, expressionLevel) = functionInputResolver().createOneProteinOneIntegerInput(function)
            ProteinHasExactExpressionByIhc(protein, expressionLevel)
        }
    }

    private fun proteinHasSufficientExpressionByIhcCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (protein, expressionLevel) = functionInputResolver().createOneProteinOneIntegerInput(function)
            ProteinHasSufficientExpressionByIhc(protein, expressionLevel)
        }
    }

    private fun proteinIsWildTypeByIhcCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            ProteinIsWildTypeByIhc(functionInputResolver().createOneProteinInput(function).proteinName)
        }
    }

    private fun hasAvailableProteinExpressionCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasAvailableProteinExpression(functionInputResolver().createOneProteinInput(function).proteinName)
        }
    }

    private fun hasPositiveHER2ExpressionByIhcCreator(): FunctionCreator {
        return { HasPositiveHER2ExpressionByIhc(maxMolecularTestAge()) }
    }

    private fun proteinHasLimitedExpressionByIhcCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (protein, expressionLevel) = functionInputResolver().createOneProteinOneIntegerInput(function)
            ProteinHasLimitedExpressionByIhc(protein, expressionLevel)
        }
    }

    private fun hasSufficientPDL1ByMeasureByIhcCreator(measure: String? = null): FunctionCreator {
        return { function: EligibilityFunction ->
            val minPDL1 = functionInputResolver().createOneIntegerInput(function)
            HasSufficientPDL1ByIhc(measure, minPDL1.toDouble())
        }
    }

    private fun hasLimitedPDL1ByMeasureByIhcCreator(measure: String? = null): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxPDL1 = functionInputResolver().createOneIntegerInput(function)
            HasLimitedPDL1ByIhc(measure, maxPDL1.toDouble())
        }
    }

    private fun hasSufficientPDL1ByDoubleMeasureByIhcCreator(measure: String, doidModel: DoidModel? = null): FunctionCreator {
        return { function: EligibilityFunction ->
            val minPDL1 = functionInputResolver().createOneDoubleInput(function)
            HasSufficientPDL1ByIhc(measure, minPDL1, doidModel)
        }
    }

    private fun hasLimitedPDL1ByDoubleMeasureByIhcCreator(measure: String, doidModel: DoidModel? = null): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxPDL1 = functionInputResolver().createOneDoubleInput(function)
            HasLimitedPDL1ByIhc(measure, maxPDL1, doidModel)
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
        return {
            Or(
                listOf(
                    GeneHasVariantWithProteinImpact("EGFR", EGFR_PACC_PROTEIN_IMPACTS, maxMolecularTestAge()),
                    GeneHasVariantInCodon("EGFR", EGFR_PACC_CODON_VARIANTS, maxMolecularTestAge())
                )
            )
        }
    }

    private fun hasCoDeletionOfChromosomeArmsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (chromosome1, chromosome2) = functionInputResolver().createTwoStringsInput(function)
            HasCodeletionOfChromosomeArms(chromosome1, chromosome2)
        }
    }

    private fun hasProteinPolymorphismCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (protein, polymorphism) = functionInputResolver().createOneProteinOneStringInput(function)
            ProteinHasPolymorphism(protein, polymorphism)
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