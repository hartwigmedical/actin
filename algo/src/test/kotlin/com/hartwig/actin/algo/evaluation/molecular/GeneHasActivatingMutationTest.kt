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
    private val gene = "gene A"
    private val function = GeneHasActivatingMutation(gene)
    private val activatingVariant: Variant = TestVariantFactory.builder()
        .gene(gene)
        .isReportable(true)
        .driverLikelihood(DriverLikelihood.HIGH)
        .geneRole(GeneRole.ONCO)
        .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
        .isHotspot(true)
        .isAssociatedWithDrugResistance(false)
        .clonalLikelihood(0.8)
        .build()

    @Test
    fun shouldFailForMinimalPatient() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }

    @Test
    fun shouldPassWithActivatingMutationForGene() {
        assertResultForVariant(EvaluationResult.PASS, activatingVariant)
    }

    @Test
    fun shouldFailWithActivatingMutationForOtherGene() {
        assertResultForVariant(
            EvaluationResult.FAIL, TestVariantFactory.builder()
                .from(activatingVariant)
                .gene("gene B")
                .build()
        )
    }

    @Test
    fun shouldWarnWithActivatingMutationForTSGGene() {
        assertResultForVariant(
            EvaluationResult.WARN, TestVariantFactory.builder()
                .from(activatingVariant)
                .geneRole(GeneRole.TSG)
                .build()
        )
    }

    @Test
    fun shouldWarnWithActivatingMutationWithDrugResistanceForGene() {
        assertResultForVariant(
            EvaluationResult.WARN, TestVariantFactory.builder()
                .from(activatingVariant)
                .isAssociatedWithDrugResistance(true)
                .build()
        )
    }

    @Test
    fun shouldWarnWithActivatingMutationForGeneWithNoProteinEffectOrHotspot() {
        assertResultForVariant(
            EvaluationResult.WARN, TestVariantFactory.builder()
                .from(activatingVariant)
                .proteinEffect(ProteinEffect.UNKNOWN)
                .isHotspot(false)
                .build()
        )
    }

    @Test
    fun shouldWarnWithActivatingMutationForGeneWithLowDriverLikelihood() {
        assertResultForVariant(
            EvaluationResult.WARN, TestVariantFactory.builder()
                .from(activatingVariant)
                .driverLikelihood(DriverLikelihood.LOW)
                .build()
        )
    }

    @Test
    fun shouldWarnWithActivatingMutationForGeneWithLowDriverLikelihoodAndUnknownProteinEffectAndUnknownTML() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.builder()
                        .from(activatingVariant)
                        .proteinEffect(ProteinEffect.UNKNOWN)
                        .driverLikelihood(DriverLikelihood.LOW)
                        .build()
                )
            )
        )
    }

    @Test
    fun shouldWarnWithNonReportableMissenseMutationForGene() {
        assertResultForVariant(
            EvaluationResult.WARN, TestVariantFactory.builder()
                .gene(gene)
                .isReportable(false)
                .isHotspot(false)
                .canonicalImpact(TestTranscriptImpactFactory.builder().codingEffect(CodingEffect.MISSENSE).build())
                .build()
        )
    }

    @Test
    fun shouldWarnWithNonReportableHotspotMutationForGene() {
        assertResultForVariant(
            EvaluationResult.WARN, TestVariantFactory.builder()
                .gene(gene)
                .isReportable(false)
                .isHotspot(true)
                .build()
        )
    }

    @Test
    fun shouldWarnWithHighDriverSubclonalActivatingMutationForGene() {
        assertResultForVariant(
            EvaluationResult.WARN, TestVariantFactory.builder()
                .gene(gene)
                .isReportable(true)
                .driverLikelihood(DriverLikelihood.HIGH)
                .clonalLikelihood(0.2)
                .build()
        )
    }

    @Test
    fun shouldWarnWithLowDriverSubclonalActivatingMutationForGene() {
        assertResultForVariant(
            EvaluationResult.WARN, TestVariantFactory.builder()
                .gene(gene)
                .isReportable(true)
                .driverLikelihood(DriverLikelihood.LOW)
                .clonalLikelihood(0.2)
                .build()
        )
    }

    @Test
    fun shouldPassWithHighDriverActivatingMutationWithHighTML() {
        assertResultForVariant(EvaluationResult.PASS, activatingVariant)
    }

    @Test
    fun shouldFailWithLowDriverActivatingMutationWithHighTMLAndUnknownProteinEffect() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariants(
                    true, TestVariantFactory.builder()
                        .from(activatingVariant)
                        .proteinEffect(ProteinEffect.UNKNOWN)
                        .driverLikelihood(DriverLikelihood.LOW)
                        .build()
                )
            )
        )
    }

    @Test
    fun shouldWarnWithLowDriverActivatingMutationWithLowTMLAndUnknownProteinEffect() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariants(
                    false,
                    TestVariantFactory.builder()
                        .from(activatingVariant)
                        .proteinEffect(ProteinEffect.UNKNOWN)
                        .driverLikelihood(DriverLikelihood.LOW)
                        .build()
                )
            )
        )
    }

    private fun assertResultForVariant(expectedResult: EvaluationResult, variant: Variant) {
        assertMolecularEvaluation(expectedResult, function.evaluate(MolecularTestFactory.withVariant(variant)))

        // Repeat with high TML since unknown TML always results in a warning for reportable variants:
        assertMolecularEvaluation(
            expectedResult,
            function.evaluate(MolecularTestFactory.withHasTumorMutationalLoadAndVariants(true, variant))
        )
    }
}