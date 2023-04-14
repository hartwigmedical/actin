package com.hartwig.actin.soc.evaluation.molecular

import com.google.common.collect.Lists

class MolecularRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        val map: MutableMap<EligibilityRule, FunctionCreator> = Maps.newHashMap()
        map[EligibilityRule.DRIVER_EVENT_IN_ANY_GENES_X_WITH_APPROVED_THERAPY_AVAILABLE] = anyGeneHasDriverEventWithApprovedTherapyCreator()
        map[EligibilityRule.ACTIVATION_OR_AMPLIFICATION_OF_GENE_X] = geneIsActivatedOrAmplifiedCreator()
        map[EligibilityRule.INACTIVATION_OF_GENE_X] = geneIsInactivatedCreator()
        map[EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X] = geneHasActivatingMutationCreator()
        map[EligibilityRule.MUTATION_IN_GENE_X_OF_ANY_PROTEIN_IMPACTS_Y] = geneHasVariantWithAnyProteinImpactsCreator()
        map[EligibilityRule.MUTATION_IN_GENE_X_IN_ANY_CODONS_Y] = geneHasVariantInAnyCodonsCreator()
        map[EligibilityRule.MUTATION_IN_GENE_X_IN_EXON_Y] = geneHasVariantInExonCreator()
        map[EligibilityRule.MUTATION_IN_GENE_X_IN_EXON_Y_TO_EXON_Z] = geneHasVariantInExonRangeCreator()
        map[EligibilityRule.MUTATION_IN_GENE_X_IN_EXON_Y_OF_TYPE_Z] = geneHasVariantInExonOfTypeCreator()
        map[EligibilityRule.UTR_3_LOSS_IN_GENE_X] = geneHasUTR3LossCreator()
        map[EligibilityRule.AMPLIFICATION_OF_GENE_X] = geneIsAmplifiedCreator()
        map[EligibilityRule.AMPLIFICATION_OF_GENE_X_OF_AT_LEAST_Y_COPIES] = geneIsAmplifiedMinCopiesCreator()
        map[EligibilityRule.FUSION_IN_GENE_X] = hasFusionInGeneCreator()
        map[EligibilityRule.WILDTYPE_OF_GENE_X] = geneIsWildTypeCreator()
        map[EligibilityRule.EXON_SKIPPING_GENE_X_EXON_Y] = geneHasSpecificExonSkippingCreator()
        map[EligibilityRule.MSI_SIGNATURE] = isMicrosatelliteUnstableCreator
        map[EligibilityRule.HRD_SIGNATURE] = isHomologousRepairDeficientCreator
        map[EligibilityRule.TMB_OF_AT_LEAST_X] = hasSufficientTumorMutationalBurdenCreator()
        map[EligibilityRule.TML_OF_AT_LEAST_X] = hasSufficientTumorMutationalLoadCreator()
        map[EligibilityRule.TML_BETWEEN_X_AND_Y] = hasCertainTumorMutationalLoadCreator()
        map[EligibilityRule.HAS_HLA_TYPE_X] = hasSpecificHLATypeCreator()
        map[EligibilityRule.OVEREXPRESSION_OF_GENE_X] = geneIsOverexpressedCreator()
        map[EligibilityRule.NON_EXPRESSION_OF_GENE_X] = geneIsNotExpressedCreator()
        map[EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC] = proteinIsExpressedByIHCCreator()
        map[EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_EXACTLY_Y] = proteinHasExactExpressionByIHCCreator()
        map[EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_AT_LEAST_Y] = proteinHasSufficientExpressionByIHCCreator()
        map[EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_AT_MOST_Y] = proteinHasLimitedExpressionByIHCCreator()
        map[EligibilityRule.PROTEIN_X_IS_WILD_TYPE_BY_IHC] = proteinIsWildTypeByIHCCreator()
        map[EligibilityRule.PD_L1_SCORE_CPS_OF_AT_LEAST_X] = hasSufficientPDL1ByCPSByIHCCreator()
        map[EligibilityRule.PD_L1_SCORE_CPS_OF_AT_MOST_X] = hasLimitedPDL1ByCPSByIHCCreator()
        map[EligibilityRule.PD_L1_SCORE_TPS_OF_AT_MOST_X] = hasLimitedPDL1ByTPSByIHCCreator()
        map[EligibilityRule.PD_L1_STATUS_MUST_BE_AVAILABLE] = hasAvailablePDL1StatusCreator()
        map[EligibilityRule.POSITIVE_FOR_CD8_T_CELLS_BY_IHC] = positiveForCD8TCellsByIHCCreator()
        map[EligibilityRule.HAS_PSMA_POSITIVE_PET_SCAN] = hasPSMAPositivePETScanCreator()
        map[EligibilityRule.MOLECULAR_RESULTS_MUST_BE_AVAILABLE] = molecularResultsAreGenerallyAvailableCreator()
        map[EligibilityRule.MOLECULAR_TEST_MUST_HAVE_BEEN_DONE_FOR_GENE_X] = molecularResultsAreAvailableForGeneCreator()
        map[EligibilityRule.MOLECULAR_TEST_MUST_HAVE_BEEN_DONE_FOR_PROMOTER_OF_GENE_X] = molecularResultsAreAvailableForPromoterOfGeneCreator()
        return map
    }

    private fun anyGeneHasDriverEventWithApprovedTherapyCreator(): FunctionCreator {
        return FunctionCreator { function -> AnyGeneHasDriverEventWithApprovedTherapy() }
    }

    private fun geneIsActivatedOrAmplifiedCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val gene: OneGene = functionInputResolver().createOneGeneInput(function)
            Or(Lists.newArrayList(GeneHasActivatingMutation(gene.geneName()), GeneIsAmplified(gene.geneName())))
        }
    }

    private fun geneIsInactivatedCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val gene: OneGene = functionInputResolver().createOneGeneInput(function)
            GeneIsInactivated(gene.geneName())
        }
    }

    private fun geneHasActivatingMutationCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val gene: OneGene = functionInputResolver().createOneGeneInput(function)
            GeneHasActivatingMutation(gene.geneName())
        }
    }

    private fun geneHasVariantWithAnyProteinImpactsCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: OneGeneManyProteinImpacts = functionInputResolver().createOneGeneManyProteinImpactsInput(function)
            GeneHasVariantWithProteinImpact(input.geneName(), input.proteinImpacts())
        }
    }

    private fun geneHasVariantInAnyCodonsCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: OneGeneManyCodons = functionInputResolver().createOneGeneManyCodonsInput(function)
            GeneHasVariantInCodon(input.geneName(), input.codons())
        }
    }

    private fun geneHasVariantInExonCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: OneGeneOneInteger = functionInputResolver().createOneGeneOneIntegerInput(function)
            GeneHasVariantInExonRangeOfType(input.geneName(), input.integer(), input.integer(), null)
        }
    }

    private fun geneHasVariantInExonRangeCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: OneGeneTwoIntegers = functionInputResolver().createOneGeneTwoIntegersInput(function)
            GeneHasVariantInExonRangeOfType(input.geneName(), input.integer1(), input.integer2(), null)
        }
    }

    private fun geneHasVariantInExonOfTypeCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: OneGeneOneIntegerOneVariantType = functionInputResolver().createOneGeneOneIntegerOneVariantTypeInput(function)
            GeneHasVariantInExonRangeOfType(input.geneName(), input.integer(), input.integer(), input.variantType())
        }
    }

    private fun geneHasUTR3LossCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val gene: OneGene = functionInputResolver().createOneGeneInput(function)
            GeneHasUTR3Loss(gene.geneName())
        }
    }

    private fun geneIsAmplifiedCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val gene: OneGene = functionInputResolver().createOneGeneInput(function)
            GeneIsAmplified(gene.geneName())
        }
    }

    private fun geneIsAmplifiedMinCopiesCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: OneGeneOneInteger = functionInputResolver().createOneGeneOneIntegerInput(function)
            GeneIsAmplifiedMinCopies(input.geneName(), input.integer())
        }
    }

    private fun hasFusionInGeneCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val gene: OneGene = functionInputResolver().createOneGeneInput(function)
            HasFusionInGene(gene.geneName())
        }
    }

    private fun geneIsWildTypeCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val gene: OneGene = functionInputResolver().createOneGeneInput(function)
            GeneIsWildType(gene.geneName())
        }
    }

    private fun geneHasSpecificExonSkippingCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: OneGeneOneInteger = functionInputResolver().createOneGeneOneIntegerInput(function)
            GeneHasSpecificExonSkipping(input.geneName(), input.integer())
        }
    }

    private val isMicrosatelliteUnstableCreator: FunctionCreator
        private get() = FunctionCreator { function -> IsMicrosatelliteUnstable() }
    private val isHomologousRepairDeficientCreator: FunctionCreator
        private get() = FunctionCreator { function -> IsHomologousRepairDeficient() }

    private fun hasSufficientTumorMutationalBurdenCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val minTumorMutationalBurden: Double = functionInputResolver().createOneDoubleInput(function)
            HasSufficientTumorMutationalBurden(minTumorMutationalBurden)
        }
    }

    private fun hasSufficientTumorMutationalLoadCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val minTumorMutationalLoad: Int = functionInputResolver().createOneIntegerInput(function)
            HasTumorMutationalLoadWithinRange(minTumorMutationalLoad, null)
        }
    }

    private fun hasCertainTumorMutationalLoadCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: TwoIntegers = functionInputResolver().createTwoIntegersInput(function)
            HasTumorMutationalLoadWithinRange(input.integer1(), input.integer2())
        }
    }

    private fun hasSpecificHLATypeCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val hlaAlleleToFind: OneHlaAllele = functionInputResolver().createOneHlaAlleleInput(function)
            HasSpecificHLAType(hlaAlleleToFind.allele())
        }
    }

    private fun geneIsOverexpressedCreator(): FunctionCreator {
        return FunctionCreator { function -> GeneIsOverexpressed() }
    }

    private fun geneIsNotExpressedCreator(): FunctionCreator {
        return FunctionCreator { function -> GeneIsNotExpressed() }
    }

    private fun proteinIsExpressedByIHCCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val gene: String = functionInputResolver().createOneStringInput(function)
            ProteinIsExpressedByIHC(gene)
        }
    }

    private fun proteinHasExactExpressionByIHCCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: OneIntegerOneString = functionInputResolver().createOneStringOneIntegerInput(function)
            ProteinHasExactExpressionByIHC(input.string(), input.integer())
        }
    }

    private fun proteinHasSufficientExpressionByIHCCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: OneIntegerOneString = functionInputResolver().createOneStringOneIntegerInput(function)
            ProteinHasExactExpressionByIHC(input.string(), input.integer())
        }
    }

    private fun proteinIsWildTypeByIHCCreator(): FunctionCreator {
        return FunctionCreator { function -> ProteinIsWildTypeByIHC(functionInputResolver().createOneStringInput(function)) }
    }

    private fun proteinHasLimitedExpressionByIHCCreator(): FunctionCreator {
        return FunctionCreator { function -> ProteinHasLimitedExpressionByIHCCreator() }
    }

    private fun hasSufficientPDL1ByCPSByIHCCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val minPDL1: Int = functionInputResolver().createOneIntegerInput(function)
            HasSufficientPDL1ByIHC("CPS", minPDL1)
        }
    }

    private fun hasLimitedPDL1ByCPSByIHCCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val maxPDL1: Int = functionInputResolver().createOneIntegerInput(function)
            HasLimitedPDL1ByIHC("CPS", maxPDL1)
        }
    }

    private fun hasLimitedPDL1ByTPSByIHCCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val maxPDL1Percentage: Double = functionInputResolver().createOneDoubleInput(function)
            HasLimitedPDL1ByIHC("TPS", maxPDL1Percentage)
        }
    }

    private fun hasAvailablePDL1StatusCreator(): FunctionCreator {
        return FunctionCreator { function -> HasAvailablePDL1Status() }
    }

    private fun positiveForCD8TCellsByIHCCreator(): FunctionCreator {
        return FunctionCreator { function -> PositiveForCD8TCellsByIHC() }
    }

    private fun hasPSMAPositivePETScanCreator(): FunctionCreator {
        return FunctionCreator { function -> HasPSMAPositivePETScan() }
    }

    private fun molecularResultsAreGenerallyAvailableCreator(): FunctionCreator {
        return FunctionCreator { function -> MolecularResultsAreGenerallyAvailable() }
    }

    private fun molecularResultsAreAvailableForGeneCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val gene: OneGene = functionInputResolver().createOneGeneInput(function)
            MolecularResultsAreAvailableForGene(gene.geneName())
        }
    }

    private fun molecularResultsAreAvailableForPromoterOfGeneCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val gene: OneGene = functionInputResolver().createOneGeneInput(function)
            MolecularResultsAreAvailableForPromoterOfGene(gene.geneName())
        }
    }
}