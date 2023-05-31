package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.driver.Variant
import org.junit.Test


class GeneHasActivatingMutationTest {
    private val function = GeneHasActivatingMutation(GENE)

    @Test
    fun shouldFailForMinimalPatient() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }

    @Test
    fun shouldPassWithActivatingMutationForGene() {
        assertResultForVariant(EvaluationResult.PASS, ACTIVATING_VARIANT)
    }

    @Test
    fun shouldFailWithActivatingMutationForOtherGene() {
        assertResultForVariant(
            EvaluationResult.FAIL, TestVariantFactory.builder().from(ACTIVATING_VARIANT).gene("gene B").build()
        )
    }

    @Test
    fun shouldWarnWithActivatingMutationForTSGGene() {
        assertResultForVariant(
            EvaluationResult.WARN, TestVariantFactory.builder().from(ACTIVATING_VARIANT).geneRole(GeneRole.TSG).build()
        )
    }

    @Test
    fun shouldWarnWithActivatingMutationWithDrugResistanceForGene() {
        assertResultForVariant(
            EvaluationResult.WARN, TestVariantFactory.builder().from(ACTIVATING_VARIANT).isAssociatedWithDrugResistance(true).build()
        )
    }

    @Test
    fun shouldWarnWithActivatingMutationForGeneWithNoProteinEffectOrHotspot() {
        assertResultForVariant(
            EvaluationResult.WARN,
            TestVariantFactory.builder().from(ACTIVATING_VARIANT).proteinEffect(ProteinEffect.UNKNOWN).isHotspot(false).build()
        )
    }

    @Test
    fun shouldWarnWithActivatingMutationForGeneWithLowDriverLikelihood() {
        assertResultForVariant(
            EvaluationResult.WARN, TestVariantFactory.builder().from(ACTIVATING_VARIANT).driverLikelihood(DriverLikelihood.LOW).build()
        )
    }

    @Test
    fun shouldWarnWithActivatingMutationForGeneWithLowDriverLikelihoodAndUnknownProteinEffectAndUnknownTML() {
        assertResultForVariantWithTML(
            EvaluationResult.WARN,
            TestVariantFactory.builder().from(ACTIVATING_VARIANT).proteinEffect(ProteinEffect.UNKNOWN)
                .driverLikelihood(DriverLikelihood.LOW).build(),
            null
        )
    }

    @Test
    fun shouldWarnWithNonReportableMissenseMutationForGene() {
        assertResultForVariant(
            EvaluationResult.WARN,
            TestVariantFactory.builder().gene(GENE).isReportable(false).isHotspot(false)
                .canonicalImpact(TestTranscriptImpactFactory.builder().codingEffect(CodingEffect.MISSENSE).build()).build()
        )
    }

    @Test
    fun shouldWarnWithNonReportableHotspotMutationForGene() {
        assertResultForVariant(
            EvaluationResult.WARN, TestVariantFactory.builder().gene(GENE).isReportable(false).isHotspot(true).build()
        )
    }

    @Test
    fun shouldWarnWithHighDriverSubclonalActivatingMutationForGene() {
        assertResultForVariant(
            EvaluationResult.WARN,
            TestVariantFactory.builder().gene(GENE).isReportable(true).driverLikelihood(DriverLikelihood.HIGH).clonalLikelihood(0.2).build()
        )
    }

    @Test
    fun shouldWarnWithLowDriverSubclonalActivatingMutationForGeneAndUnknownTML() {
        assertResultForVariantWithTML(
            EvaluationResult.WARN,
            TestVariantFactory.builder().gene(GENE).isReportable(true).driverLikelihood(DriverLikelihood.LOW).clonalLikelihood(0.2).build(),
            null
        )
    }

    @Test
    fun shouldFailWithLowDriverSubclonalActivatingMutationForGeneAndHighTML() {
        assertResultForVariantWithTML(
            EvaluationResult.FAIL,
            TestVariantFactory.builder().gene(GENE).isReportable(true).driverLikelihood(DriverLikelihood.LOW).clonalLikelihood(0.2).build(),
            true
        )
    }

    @Test
    fun shouldPassWithHighDriverActivatingMutationWithHighTML() {
        assertResultForVariant(EvaluationResult.PASS, ACTIVATING_VARIANT)
    }

    @Test
    fun shouldFailWithLowDriverActivatingMutationWithHighTMLAndUnknownProteinEffect() {
        assertResultForVariantWithTML(
            EvaluationResult.FAIL,
            TestVariantFactory.builder().from(ACTIVATING_VARIANT).proteinEffect(ProteinEffect.UNKNOWN)
                .driverLikelihood(DriverLikelihood.LOW).build(),
            true
        )
    }

    @Test
    fun shouldWarnWithLowDriverActivatingMutationWithLowTMLAndUnknownProteinEffect() {
        assertResultForVariantWithTML(
            EvaluationResult.WARN,
            TestVariantFactory.builder().from(ACTIVATING_VARIANT).proteinEffect(ProteinEffect.UNKNOWN)
                .driverLikelihood(DriverLikelihood.LOW).build(),
            false
        )
    }

    private fun assertResultForVariant(expectedResult: EvaluationResult, variant: Variant) {
        assertResultForVariantWithTML(expectedResult, variant, null)

        // Repeat with high TML since unknown TML always results in a warning for reportable variants:
        assertResultForVariantWithTML(expectedResult, variant, true)
    }

    private fun assertResultForVariantWithTML(expectedResult: EvaluationResult, variant: Variant, hasHighTML: Boolean?) {
        assertMolecularEvaluation(
            expectedResult, function.evaluate(MolecularTestFactory.withHasTumorMutationalLoadAndVariants(hasHighTML, variant))
        )
    }

    companion object {
        private const val GENE = "gene A"
        private val ACTIVATING_VARIANT: Variant =
            TestVariantFactory.builder().gene(GENE).isReportable(true).driverLikelihood(DriverLikelihood.HIGH).geneRole(GeneRole.ONCO)
                .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION).isHotspot(true).isAssociatedWithDrugResistance(false).clonalLikelihood(0.8)
                .build()
    }
}