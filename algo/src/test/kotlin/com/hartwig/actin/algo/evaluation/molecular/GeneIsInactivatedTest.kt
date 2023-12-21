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
        TestHomozygousDisruptionFactory.createMinimal().gene(GENE).isReportable(true).geneRole(GeneRole.TSG)
            .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION).build()

    private val matchingLoss: CopyNumber =
        TestCopyNumberFactory.createMinimal().gene(GENE).isReportable(true).geneRole(GeneRole.TSG)
            .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
            .type(CopyNumberType.LOSS).build()

    private val matchingVariant: Variant =
        TestVariantFactory.createMinimal().gene(GENE).isReportable(true).driverLikelihood(DriverLikelihood.HIGH).isBiallelic(true)
            .clonalLikelihood(1.0).geneRole(GeneRole.TSG).proteinEffect(ProteinEffect.LOSS_OF_FUNCTION).canonicalImpact(
                TestTranscriptImpactFactory.createMinimal().codingEffect(GeneIsInactivated.INACTIVATING_CODING_EFFECTS.iterator().next())
                    .build()
            ).build()

    @Test
    fun shouldFailWithoutAnyAlterations() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }

    @Test
    fun shouldPassWithMatchingTSGHomozygousDisruption() {
        assertMolecularEvaluation(
            EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withHomozygousDisruption(matchingHomDisruption))
        )
    }

    @Test
    fun shouldWarnWhenTSGHomozygousDisruptionIsNotReportable() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withHomozygousDisruption(
                    ImmutableHomozygousDisruption.copyOf(matchingHomDisruption).withIsReportable(false)
                )
            )
        )
    }

    @Test
    fun shouldWarnWhenHomozygouslyDisruptedGeneIsAnOncogene() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withHomozygousDisruption(
                    ImmutableHomozygousDisruption.copyOf(matchingHomDisruption).withGeneRole(GeneRole.ONCO)
                )
            )
        )
    }

    @Test
    fun shouldWarnWhenTSGHomozygousDisruptionImpliesGainOfFunction() {
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
    fun shouldWarnWhenTSGLossIsNotReportable() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withCopyNumber(ImmutableCopyNumber.copyOf(matchingLoss).withIsReportable(false))
            )
        )
    }

    @Test
    fun shouldWarnWhenLostGeneIsAnOncogene() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withCopyNumber(ImmutableCopyNumber.copyOf(matchingLoss).withGeneRole(GeneRole.ONCO))
            )
        )
    }

    @Test
    fun shouldWarnWhenLostGeneImpliesGainOfFunction() {
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
    fun shouldWarnWhenTSGVariantIsNotReportable() {
        assertResultForVariant(EvaluationResult.WARN, ImmutableVariant.copyOf(matchingVariant).withIsReportable(false))
    }

    @Test
    fun shouldWarnWhenVariantAffectsOncogene() {
        assertResultForVariant(EvaluationResult.WARN, ImmutableVariant.copyOf(matchingVariant).withGeneRole(GeneRole.ONCO))
    }

    @Test
    fun shouldWarnWhenTSGVariantImpliesGainOfFunction() {
        assertResultForVariant(
            EvaluationResult.WARN, ImmutableVariant.copyOf(matchingVariant).withProteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
        )
    }

    @Test
    fun shouldWarnWhenTSGVariantHasNoHighDriverLikelihood() {
        assertResultForVariant(
            EvaluationResult.WARN, ImmutableVariant.copyOf(matchingVariant).withDriverLikelihood(DriverLikelihood.MEDIUM)
        )
    }

    @Test
    fun shouldWarnWhenTSGVariantIsNotBiallelic() {
        assertResultForVariant(EvaluationResult.WARN, ImmutableVariant.copyOf(matchingVariant).withIsBiallelic(false))
    }

    @Test
    fun shouldWarnWhenTSGVariantIsSubclonal() {
        assertResultForVariant(EvaluationResult.WARN, ImmutableVariant.copyOf(matchingVariant).withClonalLikelihood(0.4))
    }

    @Test
    fun shouldFailWhenTSGVariantHasNoCodingImpact() {
        assertResultForVariant(
            EvaluationResult.FAIL, ImmutableVariant.copyOf(matchingVariant).withCanonicalImpact(
                TestTranscriptImpactFactory.createMinimal().codingEffect(CodingEffect.NONE).build()
            )
        )
    }

    @Test
    fun shouldPassWhenTSGVariantInHighTMLSample() {
        assertResultForMutationalLoadAndVariant(
            EvaluationResult.PASS,
            true,
            TestVariantFactory.createMinimal().from(matchingVariant).build()
        )
    }

    @Test
    fun shouldFailWhenTSGVariantHasNoHighDriverLikelihoodInHighTMLSample() {
        assertResultForMutationalLoadAndVariant(
            EvaluationResult.FAIL, true, ImmutableVariant.copyOf(matchingVariant).withDriverLikelihood(DriverLikelihood.LOW)
        )
    }

    @Test
    fun shouldWarnWhenTSGVariantIsNonBiallelicAndNonHighDriverInLowTMLSample() {
        assertResultForMutationalLoadAndVariant(
            EvaluationResult.WARN,
            false,
            ImmutableVariant.copyOf(matchingVariant).withDriverLikelihood(DriverLikelihood.LOW).withIsBiallelic(false)
        )

    }

    @Test
    fun shouldWarnWhenTSGVariantIsNonHighDriverButBiallelicInLowTMLSample() {
        assertResultForMutationalLoadAndVariant(
            EvaluationResult.WARN,
            false,
            TestVariantFactory.createMinimal().from(matchingVariant).driverLikelihood(DriverLikelihood.LOW).build()
        )
    }

    @Test
    fun shouldFailWithMultipleLowDriverVariantsWithOverlappingPhaseGroupsAndInactivatingEffects() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariants(
                    true, variantWithPhaseGroups(setOf(1)), variantWithPhaseGroups(setOf(1, 2))
                )
            )
        )
    }

    @Test
    fun shouldWarnWithMultipleLowDriverVariantsWithNonOverlappingPhaseGroupsAndInactivatingEffects() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariants(
                    true, variantWithPhaseGroups(setOf(1)), variantWithPhaseGroups(setOf(2))
                )
            )
        )
    }

    @Test
    fun shouldWarnWithMultipleLowDriverVariantsWithUnknownPhaseGroupsAndInactivatingEffects() {
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
    fun shouldWarnWithLowDriverVariantWithInactivatingEffectAndLowDriverDisruption() {
        val disruption: Disruption =
            TestDisruptionFactory.createMinimal().gene(GENE).isReportable(true).clusterGroup(1).driverLikelihood(DriverLikelihood.LOW)
                .build()
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

    private fun variantWithPhaseGroups(phaseGroups: Set<Int>?): Variant = TestVariantFactory.createMinimal().gene(GENE).isReportable(true)
        .canonicalImpact(TestTranscriptImpactFactory.createMinimal().codingEffect(CodingEffect.NONSENSE_OR_FRAMESHIFT).build())
        .driverLikelihood(DriverLikelihood.LOW)
        .phaseGroups(phaseGroups).build()
}