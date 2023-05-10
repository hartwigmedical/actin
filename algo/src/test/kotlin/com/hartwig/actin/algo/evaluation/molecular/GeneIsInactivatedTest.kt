package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.driver.*
import org.junit.Test

private const val GENE = "gene A"

class GeneIsInactivatedTest {

    private val function = GeneIsInactivated(GENE)

    private val matchingHomDisruption: HomozygousDisruption =
        TestHomozygousDisruptionFactory.builder().gene(GENE).isReportable(true).geneRole(GeneRole.TSG)
            .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION).build()

    private val matchingLoss: CopyNumber =
        TestCopyNumberFactory.builder().gene(GENE).isReportable(true).geneRole(GeneRole.TSG).proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
            .type(CopyNumberType.LOSS).build()

    private val matchingVariant: Variant =
        TestVariantFactory.builder().gene(GENE).isReportable(true).driverLikelihood(DriverLikelihood.HIGH).isBiallelic(true)
            .clonalLikelihood(1.0).geneRole(GeneRole.TSG).proteinEffect(ProteinEffect.LOSS_OF_FUNCTION).canonicalImpact(
                TestTranscriptImpactFactory.builder().codingEffect(GeneIsInactivated.INACTIVATING_CODING_EFFECTS.iterator().next()).build()
            ).build()

    @Test
    fun shouldFailWithNoMatchingGeneAlterations() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }

    @Test
    fun shouldPassWithMatchingTSGHomozygousDisruption() {
        assertMolecularEvaluation(
            EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withHomozygousDisruption(matchingHomDisruption))
        )
    }

    @Test
    fun shouldWarnWithMatchingUnreportableTSGHomozygousDisruption() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withHomozygousDisruption(
                    ImmutableHomozygousDisruption.copyOf(matchingHomDisruption).withIsReportable(false)
                )
            )
        )
    }

    @Test
    fun shouldWarnWithMatchingOncoHomozygousDisruption() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withHomozygousDisruption(
                    ImmutableHomozygousDisruption.copyOf(matchingHomDisruption).withGeneRole(GeneRole.ONCO)
                )
            )
        )
    }

    @Test
    fun shouldWarnWithMatchingTSGHomozygousDisruptionWithGainOfFunctionEffect() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withHomozygousDisruption(
                    ImmutableHomozygousDisruption.copyOf(matchingHomDisruption).withProteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                )
            )
        )
    }

    @Test
    fun shouldPassWithMatchingTSGLoss() {
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withCopyNumber(matchingLoss)))
    }

    @Test
    fun shouldWarnWithMatchingUnreportableTSGLoss() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withCopyNumber(ImmutableCopyNumber.copyOf(matchingLoss).withIsReportable(false))
            )
        )
    }

    @Test
    fun shouldWarnWithMatchingOncoLoss() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withCopyNumber(ImmutableCopyNumber.copyOf(matchingLoss).withGeneRole(GeneRole.ONCO))
            )
        )
    }

    @Test
    fun shouldWarnWithMatchingTSGLossWithGainOfFunctionEffect() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withCopyNumber(
                    ImmutableCopyNumber.copyOf(matchingLoss).withProteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                )
            )
        )
    }

    @Test
    fun shouldPassWithMatchingTSGVariant() {
        assertResultForVariant(EvaluationResult.PASS, matchingVariant)
    }

    @Test
    fun shouldWarnWithMatchingUnreportableTSGVariant() {
        assertResultForVariant(EvaluationResult.WARN, ImmutableVariant.copyOf(matchingVariant).withIsReportable(false))
    }

    @Test
    fun shouldWarnWithMatchingOncoVariant() {
        assertResultForVariant(EvaluationResult.WARN, ImmutableVariant.copyOf(matchingVariant).withGeneRole(GeneRole.ONCO))
    }

    @Test
    fun shouldWarnWithMatchingTSGVariantWithGainOfFunctionEffect() {
        assertResultForVariant(
            EvaluationResult.WARN, ImmutableVariant.copyOf(matchingVariant).withProteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
        )
    }

    @Test
    fun shouldWarnWithMatchingTSGVariantWithMediumDriverLikelihood() {
        assertResultForVariant(
            EvaluationResult.WARN, ImmutableVariant.copyOf(matchingVariant).withDriverLikelihood(DriverLikelihood.MEDIUM)
        )
    }

    @Test
    fun shouldWarnWithNonBiallelicMatchingTSGVariant() {
        assertResultForVariant(EvaluationResult.WARN, ImmutableVariant.copyOf(matchingVariant).withIsBiallelic(false))
    }

    @Test
    fun shouldWarnWithMatchingTSGVariantWithLowerClonalLikelihood() {
        assertResultForVariant(EvaluationResult.WARN, ImmutableVariant.copyOf(matchingVariant).withClonalLikelihood(0.4))
    }

    @Test
    fun shouldFailWithMatchingTSGVariantWithNoCodingEffect() {
        assertResultForVariant(
            EvaluationResult.FAIL, ImmutableVariant.copyOf(matchingVariant).withCanonicalImpact(
                TestTranscriptImpactFactory.builder().codingEffect(CodingEffect.NONE).build()
            )
        )
    }

    @Test
    fun shouldPassWithMatchingTSGVariantAndHighTMLAndHighDriverLikelihood() {
        assertResultForMutationalLoadAndVariant(EvaluationResult.PASS, true, TestVariantFactory.builder().from(matchingVariant).build())
    }

    @Test
    fun shouldFailWithMatchingTSGVariantAndHighTMLAndLowDriverLikelihood() {
        assertResultForMutationalLoadAndVariant(
            EvaluationResult.FAIL, true, ImmutableVariant.copyOf(matchingVariant).withDriverLikelihood(DriverLikelihood.LOW)
        )
    }

    @Test
    fun shouldWarnWithNonBiallelicMatchingTSGVariantAndLowTMLAndLowDriverLikelihood() {
        assertResultForMutationalLoadAndVariant(
            EvaluationResult.WARN,
            false,
            ImmutableVariant.copyOf(matchingVariant).withDriverLikelihood(DriverLikelihood.LOW).withIsBiallelic(false)
        )

    }

    @Test
    fun shouldWarnWithBiallelicMatchingTSGVariantAndLowTMLAndLowDriverLikelihood() {
        assertResultForMutationalLoadAndVariant(
            EvaluationResult.WARN, false, TestVariantFactory.builder().from(matchingVariant).driverLikelihood(DriverLikelihood.LOW).build()
        )
    }

    @Test
    fun shouldFailWithSingleMatchingVariantWithInactivatingEffect() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withHasTumorMutationalLoadAndVariants(true, variantWithPhaseGroups(setOf(1))))
        )
    }

    @Test
    fun shouldFailWithMultipleMatchingVariantsWithOverlappingPhaseGroupsAndInactivatingEffects() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariants(
                    true, variantWithPhaseGroups(setOf(1)), variantWithPhaseGroups(setOf(1, 2))
                )
            )
        )
    }

    @Test
    fun shouldWarnWithMultipleMatchingVariantsWithNonOverlappingPhaseGroupsAndInactivatingEffects() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariants(
                    true, variantWithPhaseGroups(setOf(1)), variantWithPhaseGroups(setOf(2))
                )
            )
        )
    }

    @Test
    fun shouldWarnWithMultipleMatchingVariantsWithUnknownPhaseGroupsAndInactivatingEffects() {
        val variant1 = variantWithPhaseGroups(null)
        // Add copy number to make distinct:
        val variant2 = ImmutableVariant.copyOf(variant1).withVariantCopyNumber(1.0)

        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariants(true, variant1, variant2)
            )
        )
    }

    @Test
    fun shouldWarnWithMatchingVariantWithInactivatingEffectAndMatchingDisruption() {
        val disruption: Disruption = TestDisruptionFactory.builder().gene(GENE).isReportable(true).clusterGroup(1).build()
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariantAndDisruption(
                    true, variantWithPhaseGroups(setOf(1)), disruption
                )
            )
        )
    }

    private fun assertResultForVariant(result: EvaluationResult, variant: Variant) {
        assertMolecularEvaluation(result, function.evaluate(MolecularTestFactory.withVariant(variant)))
    }

    private fun assertResultForMutationalLoadAndVariant(
        result: EvaluationResult, hasHighTumorMutationalLoad: Boolean, variant: ImmutableVariant
    ) {
        assertMolecularEvaluation(
            result, function.evaluate(MolecularTestFactory.withHasTumorMutationalLoadAndVariants(hasHighTumorMutationalLoad, variant))
        )
    }

    private fun variantWithPhaseGroups(phaseGroups: Set<Int>?): Variant = TestVariantFactory.builder().gene(GENE).isReportable(true)
        .canonicalImpact(TestTranscriptImpactFactory.builder().codingEffect(CodingEffect.NONSENSE_OR_FRAMESHIFT).build())
        .phaseGroups(phaseGroups).build()
}