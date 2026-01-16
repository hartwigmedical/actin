package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.evaluation.composite.Or
import com.hartwig.actin.datamodel.trial.DoubleParameter
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.GeneParameter
import com.hartwig.actin.datamodel.trial.HaplotypeParameter
import com.hartwig.actin.datamodel.trial.HlaGroupParameter
import com.hartwig.actin.datamodel.trial.IntegerParameter
import com.hartwig.actin.datamodel.trial.ManyCodonsParameter
import com.hartwig.actin.datamodel.trial.ManyGenesParameter
import com.hartwig.actin.datamodel.trial.ManyHlaAllelesParameter
import com.hartwig.actin.datamodel.trial.ManyProteinImpactsParameter
import com.hartwig.actin.datamodel.trial.Parameter
import com.hartwig.actin.datamodel.trial.ProteinParameter
import com.hartwig.actin.datamodel.trial.StringParameter
import com.hartwig.actin.datamodel.trial.VariantTypeParameter
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.trial.input.EligibilityRule

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
                    hasMolecularDriverEventWithApprovedTherapyAvailableCreator(),
            EligibilityRule.HAS_MOLECULAR_DRIVER_EVENT_IN_ANY_GENES_X_WITH_APPROVED_THERAPY_AVAILABLE to
                    hasMolecularDriverEventInSomeGenesWithApprovedTherapyAvailableCreator(),
            EligibilityRule.HAS_MOLECULAR_DRIVER_EVENT_IN_NSCLC to
                    { HasMolecularDriverEventInNsclc(null, emptySet(), false, false) },
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
            EligibilityRule.HAS_DELETION_OF_MTAP to { HasMtapDeletion() },
            EligibilityRule.ACTIVATION_OR_AMPLIFICATION_OF_GENE_X to geneIsActivatedOrAmplifiedCreator(),
            EligibilityRule.INACTIVATION_OF_GENE_X to geneIsInactivatedCreator(onlyDeletions = false),
            EligibilityRule.DELETION_OF_GENE_X to geneIsInactivatedCreator(onlyDeletions = true),
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
            EligibilityRule.MMR_DEFICIENT to { IsMmrDeficient() },
            EligibilityRule.HRD_SIGNATURE to { IsHomologousRecombinationDeficient() },
            EligibilityRule.HRD_SIGNATURE_WITHOUT_MUTATION_OR_WITH_VUS_MUTATION_IN_GENES_X to isHomologousRecombinationDeficientWithoutMutationOrWithVUSMutationInGenesXCreator(),
            EligibilityRule.HRD_SIGNATURE_WITHOUT_MUTATION_IN_GENES_X to isHomologousRecombinationDeficientWithoutMutationInGenesXCreator(),
            EligibilityRule.TMB_OF_AT_LEAST_X to hasSufficientTumorMutationalBurdenCreator(),
            EligibilityRule.TML_OF_AT_LEAST_X to hasSufficientTumorMutationalLoadCreator(),
            EligibilityRule.TML_BETWEEN_X_AND_Y to hasCertainTumorMutationalLoadCreator(),
            EligibilityRule.HAS_ANY_HLA_TYPE_X to hasAnyHLATypeCreator(),
            EligibilityRule.HAS_HLA_GROUP_X to hasSpecificHLAGroupCreator(),
            EligibilityRule.HAS_UGT1A1_HAPLOTYPE_X to hasUGT1A1HaplotypeCreator(),
            EligibilityRule.HAS_HOMOZYGOUS_DPYD_DEFICIENCY to { HasHomozygousDPYDDeficiency() },
            EligibilityRule.HAS_HETEROZYGOUS_DPYD_DEFICIENCY to { HasHeterozygousDPYDDeficiency() },
            EligibilityRule.HAS_KNOWN_HPV_STATUS to { HasKnownHPVStatus() },
            EligibilityRule.OVEREXPRESSION_OF_ANY_GENE_X to anyGeneFromSetIsOverExpressedCreator(),
            EligibilityRule.NON_EXPRESSION_OF_ANY_GENE_X to anyGeneFromSetIsNotExpressedCreator(),
            EligibilityRule.SPECIFIC_MRNA_EXPRESSION_REQUIREMENTS_MET_FOR_GENES_X to genesFromSetMeetMrnaExpressionRequirementsCreator(),
            EligibilityRule.LOSS_OF_PROTEIN_X_BY_IHC to proteinIsLostByIhcCreator(),
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC to proteinIsExpressedByIhcCreator(),
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_EXACTLY_Y to proteinHasExactExpressionByIhcCreator(),
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_AT_LEAST_Y to proteinHasSufficientExpressionByIhcCreator(),
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_AT_MOST_Y to proteinHasLimitedExpressionByIhcCreator(),
            EligibilityRule.PROTEIN_X_IS_WILD_TYPE_BY_IHC to proteinIsWildTypeByIhcCreator(),
            EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_MUST_BE_AVAILABLE to hasAvailableProteinExpressionCreator(),
            EligibilityRule.HER2_IHC_STATUS_IS_X to hasHER2ExpressionByIhcCreator(),
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
            EligibilityRule.MMR_STATUS_IS_AVAILABLE to { MmrStatusIsAvailable() },
            EligibilityRule.HAS_KNOWN_NSCLC_DRIVER_GENE_STATUSES to { NsclcDriverGeneStatusesAreAvailable() },
            EligibilityRule.HAS_EGFR_PACC_MUTATION to hasEgfrPaccMutationCreator(),
            EligibilityRule.HAS_CODELETION_OF_CHROMOSOME_ARMS_X_AND_Y to hasCoDeletionOfChromosomeArmsCreator(),
            EligibilityRule.HAS_PROTEIN_X_POLYMORPHISM_Y to hasProteinPolymorphismCreator()
        )
    }

    private fun hasMolecularDriverEventWithApprovedTherapyAvailableCreator(): FunctionCreator {
        return {
            AnyGeneHasDriverEventWithApprovedTherapy(
                null,
                doidModel(),
                EvaluationFunctionFactory.create(resources),
            )
        }
    }

    private fun hasMolecularDriverEventInSomeGenesWithApprovedTherapyAvailableCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genes = function.param<ManyGenesParameter>(0).value
            AnyGeneHasDriverEventWithApprovedTherapy(
                genes,
                doidModel(),
                EvaluationFunctionFactory.create(resources),
            )
        }
    }

    private fun hasMolecularDriverEventInNSCLCInSpecificGenesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genes = function.param<ManyGenesParameter>(0).value
            HasMolecularDriverEventInNsclc(genes, emptySet(), false, false)
        }
    }

    private fun hasMolecularDriverEventInNSCLCInAtLeastSpecificGenesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genes = function.param<ManyGenesParameter>(0).value
            HasMolecularDriverEventInNsclc(genes, emptySet(), true, false)
        }
    }

    private fun hasMolecularDriverEventInNSCLCInExcludingSomeGenesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genes = function.param<ManyGenesParameter>(0).value
            HasMolecularDriverEventInNsclc(null, genes, false, false)
        }
    }

    private fun hasMolecularEventInNSCLCWithAvailableSocAnyLineCreator(): FunctionCreator {
        return {
            HasMolecularDriverEventInNsclc(
                NSCLC_DRIVER_GENES_WITH_AVAILABLE_SOC_ANY_LINE,
                emptySet(),
                warnForMatchesOutsideGenesToInclude = false,
                withAvailableSoc = true
            )
        }
    }

    private fun hasMolecularEventInNSCLCWithAvailableSocAnyLineExcludingSomeGenesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genes = function.param<ManyGenesParameter>(0).value
            HasMolecularDriverEventInNsclc(
                NSCLC_DRIVER_GENES_WITH_AVAILABLE_SOC_ANY_LINE - genes,
                emptySet(),
                warnForMatchesOutsideGenesToInclude = false,
                withAvailableSoc = true
            )
        }
    }

    private fun hasMolecularEventInNSCLCWithAvailableSocFirstLineCreator(): FunctionCreator {
        return {
            HasMolecularDriverEventInNsclc(
                NSCLC_DRIVER_GENES_WITH_AVAILABLE_SOC_FIRST_LINE,
                emptySet(),
                warnForMatchesOutsideGenesToInclude = false,
                withAvailableSoc = true
            )
        }
    }

    private fun hasMolecularEventInNSCLCWithAvailableSocFirstLineExcludingSomeGenesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genes = function.param<ManyGenesParameter>(0).value
            HasMolecularDriverEventInNsclc(
                NSCLC_DRIVER_GENES_WITH_AVAILABLE_SOC_FIRST_LINE - genes,
                emptySet(),
                warnForMatchesOutsideGenesToInclude = false,
                withAvailableSoc = true
            )
        }
    }

    private fun geneIsActivatedOrAmplifiedCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val gene = function.param<GeneParameter>(0).value
            Or(
                listOf(
                    GeneHasActivatingMutation(gene, codonsToIgnore = null),
                    GeneIsAmplified(gene, null)
                )
            )
        }
    }

    private fun geneIsInactivatedCreator(onlyDeletions: Boolean): FunctionCreator {
        return { function: EligibilityFunction ->
            GeneIsInactivated(
                gene = function.param<GeneParameter>(0).value,
                onlyDeletions = onlyDeletions
            )
        }
    }

    private fun anyGeneHasActivatingMutationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genes = function.param<ManyGenesParameter>(0).value
            Or(genes.map { GeneHasActivatingMutation(it, codonsToIgnore = null) })
        }
    }

    private fun geneHasActivatingMutationIgnoringSomeCodonsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.GENE,
                Parameter.Type.MANY_CODONS
            )
            val gene = function.param<GeneParameter>(0).value
            val codons = function.param<ManyCodonsParameter>(1).value
            GeneHasActivatingMutation(gene, codonsToIgnore = codons)
        }
    }

    private fun anyGeneHasActivatingMutationInKinaseDomainCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genes = function.param<ManyGenesParameter>(0).value
            Or(genes.map { GeneHasActivatingMutation(it, codonsToIgnore = null, inKinaseDomain = true) })
        }
    }

    private fun geneHasVariantWithAnyProteinImpactsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.GENE,
                Parameter.Type.MANY_PROTEIN_IMPACTS
            )
            val gene = function.param<GeneParameter>(0).value
            val proteinImpacts = function.param<ManyProteinImpactsParameter>(1).value
            GeneHasVariantWithProteinImpact(gene, proteinImpacts)
        }
    }

    private fun geneHasVariantInAnyCodonsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.GENE,
                Parameter.Type.MANY_CODONS
            )
            val gene = function.param<GeneParameter>(0).value
            val codons = function.param<ManyCodonsParameter>(1).value
            GeneHasVariantInCodon(gene, codons)
        }
    }

    private fun geneHasVariantInExonCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.GENE,
                Parameter.Type.INTEGER
            )
            val gene = function.param<GeneParameter>(0).value
            val exon = function.param<IntegerParameter>(1).value
            GeneHasVariantInExonRangeOfType(gene, exon, exon, null)
        }
    }

    private fun geneHasVariantInExonRangeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.GENE,
                Parameter.Type.INTEGER,
                Parameter.Type.INTEGER
            )
            val gene = function.param<GeneParameter>(0).value
            val minExon = function.param<IntegerParameter>(1).value
            val maxExon = function.param<IntegerParameter>(2).value
            GeneHasVariantInExonRangeOfType(gene, minExon, maxExon, null)
        }
    }

    private fun geneHasVariantInExonOfTypeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.GENE,
                Parameter.Type.INTEGER,
                Parameter.Type.VARIANT_TYPE
            )
            val gene = function.param<GeneParameter>(0).value
            val exon = function.param<IntegerParameter>(1).value
            val variantType = function.param<VariantTypeParameter>(2).value
            GeneHasVariantInExonRangeOfType(gene, exon, exon, variantType)
        }
    }

    private fun geneHasUTR3LossCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            GeneHasUTR3Loss(function.param<GeneParameter>(0).value)
        }
    }

    private fun geneIsAmplifiedCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            GeneIsAmplified(function.param<GeneParameter>(0).value, null)
        }
    }

    private fun geneIsAmplifiedMinCopiesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.GENE,
                Parameter.Type.INTEGER
            )
            val gene = function.param<GeneParameter>(0).value
            val minCopies = function.param<IntegerParameter>(1).value
            GeneIsAmplified(gene, minCopies)
        }
    }

    private fun geneHasSufficientCopyNumber(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.GENE,
                Parameter.Type.INTEGER
            )
            val gene = function.param<GeneParameter>(0).value
            val minCopies = function.param<IntegerParameter>(1).value
            GeneHasSufficientCopyNumber(gene, minCopies)
        }
    }

    private fun hasFusionInGeneCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasFusionInGene(function.param<GeneParameter>(0).value)
        }
    }

    private fun geneIsWildTypeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            GeneIsWildType(function.param<GeneParameter>(0).value)
        }
    }

    private fun geneHasSpecificExonSkippingCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.GENE,
                Parameter.Type.INTEGER
            )
            val gene = function.param<GeneParameter>(0).value
            val exon = function.param<IntegerParameter>(1).value
            GeneHasSpecificExonSkipping(gene, exon)
        }
    }

    private fun hasSufficientTumorMutationalBurdenCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minTumorMutationalBurden = function.param<DoubleParameter>(0).value
            HasSufficientTumorMutationalBurden(minTumorMutationalBurden)
        }
    }

    private fun hasSufficientTumorMutationalLoadCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minTumorMutationalLoad = function.param<IntegerParameter>(0).value
            HasTumorMutationalLoadWithinRange(minTumorMutationalLoad, null)
        }
    }

    private fun hasCertainTumorMutationalLoadCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.INTEGER,
                Parameter.Type.INTEGER
            )
            val start = function.param<IntegerParameter>(0).value
            val end = function.param<IntegerParameter>(1).value
            HasTumorMutationalLoadWithinRange(start, end)
        }
    }

    private fun hasSpecificHLAGroupCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val hlaGroupToFind = function.param<HlaGroupParameter>(0).value
            HasAnyHLAType(setOf(hlaGroupToFind), matchOnHlaGroup = true)
        }
    }

    private fun hasAnyHLATypeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val hlaAllelesToFind = function.param<ManyHlaAllelesParameter>(0).value
            HasAnyHLAType(hlaAllelesToFind)
        }
    }

    private fun hasUGT1A1HaplotypeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val haplotypeToFind = function.param<HaplotypeParameter>(0).value
            HasUGT1A1Haplotype(haplotypeToFind)
        }
    }

    private fun anyGeneFromSetIsOverExpressedCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genes = function.param<ManyGenesParameter>(0).value
            AnyGeneFromSetIsOverexpressed(genes)
        }
    }

    private fun anyGeneFromSetIsNotExpressedCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val geneSet = function.param<ManyGenesParameter>(0).value
            AnyGeneFromSetIsNotExpressed(geneSet)
        }
    }

    private fun genesFromSetMeetMrnaExpressionRequirementsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genes = function.param<ManyGenesParameter>(0).value
            GenesMeetSpecificMrnaExpressionRequirements(genes)
        }
    }

    private fun proteinIsLostByIhcCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            ProteinIsLostByIhc(function.param<ProteinParameter>(0).value)
        }
    }

    private fun proteinIsExpressedByIhcCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            ProteinIsExpressedByIhc(function.param<ProteinParameter>(0).value)
        }
    }

    private fun proteinHasExactExpressionByIhcCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.PROTEIN,
                Parameter.Type.INTEGER
            )
            val protein = function.param<ProteinParameter>(0).value
            val expressionLevel = function.param<IntegerParameter>(1).value
            ProteinHasExactExpressionByIhc(protein, expressionLevel)
        }
    }

    private fun proteinHasSufficientExpressionByIhcCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.PROTEIN,
                Parameter.Type.INTEGER
            )
            val protein = function.param<ProteinParameter>(0).value
            val expressionLevel = function.param<IntegerParameter>(1).value
            ProteinHasSufficientExpressionByIhc(protein, expressionLevel)
        }
    }

    private fun proteinIsWildTypeByIhcCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            ProteinIsWildTypeByIhc(function.param<ProteinParameter>(0).value)
        }
    }

    private fun hasAvailableProteinExpressionCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasAvailableProteinExpression(function.param<ProteinParameter>(0).value)
        }
    }

    private fun hasHER2ExpressionByIhcCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val ihcTestResult = functionInputResolver().createOneIhcTestResult(function)
            HasHER2ExpressionByIhc(ihcTestResult)
        }
    }

    private fun proteinHasLimitedExpressionByIhcCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.PROTEIN,
                Parameter.Type.INTEGER
            )
            val protein = function.param<ProteinParameter>(0).value
            val expressionLevel = function.param<IntegerParameter>(1).value
            ProteinHasLimitedExpressionByIhc(protein, expressionLevel)
        }
    }

    private fun hasSufficientPDL1ByMeasureByIhcCreator(measure: String? = null): FunctionCreator {
        return { function: EligibilityFunction ->
            val minPDL1 = function.param<IntegerParameter>(0).value
            HasSufficientPDL1ByIhc(measure, minPDL1.toDouble())
        }
    }

    private fun hasLimitedPDL1ByMeasureByIhcCreator(measure: String? = null): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxPDL1 = function.param<IntegerParameter>(0).value
            HasLimitedPDL1ByIhc(measure, maxPDL1.toDouble())
        }
    }

    private fun hasSufficientPDL1ByDoubleMeasureByIhcCreator(measure: String, doidModel: DoidModel? = null): FunctionCreator {
        return { function: EligibilityFunction ->
            val minPDL1 = function.param<DoubleParameter>(0).value
            HasSufficientPDL1ByIhc(measure, minPDL1, doidModel)
        }
    }

    private fun hasLimitedPDL1ByDoubleMeasureByIhcCreator(measure: String, doidModel: DoidModel? = null): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxPDL1 = function.param<DoubleParameter>(0).value
            HasLimitedPDL1ByIhc(measure, maxPDL1, doidModel)
        }
    }

    private fun molecularResultsAreKnownForGeneCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            MolecularResultsAreKnownForGene(function.param<GeneParameter>(0).value)
        }
    }

    private fun molecularResultsAreKnownForPromoterOfGeneCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            MolecularResultsAreKnownForPromoterOfGene(function.param<GeneParameter>(0).value)
        }
    }

    private fun hasEgfrPaccMutationCreator(): FunctionCreator {
        return {
            Or(
                listOf(
                    GeneHasVariantWithProteinImpact("EGFR", EGFR_PACC_PROTEIN_IMPACTS),
                    GeneHasVariantInCodon("EGFR", EGFR_PACC_CODON_VARIANTS)
                )
            )
        }
    }

    private fun hasCoDeletionOfChromosomeArmsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.STRING,
                Parameter.Type.STRING
            )
            val chromosome1 = function.param<StringParameter>(0).value
            val chromosome2 = function.param<StringParameter>(1).value
            HasCodeletionOfChromosomeArms(chromosome1, chromosome2)
        }
    }

    private fun hasProteinPolymorphismCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.PROTEIN,
                Parameter.Type.STRING
            )
            val protein = function.param<ProteinParameter>(0).value
            val polymorphism = function.param<StringParameter>(1).value
            ProteinHasPolymorphism(protein, polymorphism)
        }
    }

    private fun isHomologousRecombinationDeficientWithoutMutationOrWithVUSMutationInGenesXCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genesToFind = function.param<ManyGenesParameter>(0).value
            IsHomologousRecombinationDeficientWithoutMutationOrWithVUSMutationInGenesX(genesToFind)
        }
    }

    private fun isHomologousRecombinationDeficientWithoutMutationInGenesXCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val genesToFind = function.param<ManyGenesParameter>(0).value
            IsHomologousRecombinationDeficientWithoutMutationInGenesX(genesToFind)
        }
    }
}
