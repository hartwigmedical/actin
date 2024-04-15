package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.driver.Variant
import org.junit.Test

private const val GENE = "gene A"

class GeneIsInactivatedTest {

    private val function = GeneIsInactivated(GENE)

    private val matchingHomDisruption = TestHomozygousDisruptionFactory.createMinimal().copy(
        gene = GENE, isReportable = true, geneRole = GeneRole.TSG, proteinEffect = ProteinEffect.LOSS_OF_FUNCTION
    )

    private val matchingLoss = TestCopyNumberFactory.createMinimal().copy(
        gene = GENE,
        isReportable = true,
        geneRole = GeneRole.TSG,
        proteinEffect = ProteinEffect.LOSS_OF_FUNCTION,
        type = CopyNumberType.LOSS
    )

    private val matchingVariant = TestVariantFactory.createMinimal().copy(
        gene = GENE,
        isReportable = true,
        driverLikelihood = DriverLikelihood.HIGH,
        isBiallelic = true,
        clonalLikelihood = 1.0,
        geneRole = GeneRole.TSG,
        proteinEffect = ProteinEffect.LOSS_OF_FUNCTION,
        canonicalImpact = TestTranscriptImpactFactory.createMinimal().copy(
            codingEffect = GeneIsInactivated.INACTIVATING_CODING_EFFECTS.first()
        )
    )

    @Test
    fun `Should fail without any alterations`() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }

    @Test
    fun `Should pass with matching TSG homozygous disruption`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withHomozygousDisruption(matchingHomDisruption))
        )
    }

    @Test
    fun `Should warn when TSG homozygous disruption is not reportable`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withHomozygousDisruption(matchingHomDisruption.copy(isReportable = false)))
        )
    }

    @Test
    fun `Should warn when homozygously disrupted gene is an oncogene`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withHomozygousDisruption(matchingHomDisruption.copy(geneRole = GeneRole.ONCO)))
        )
    }

    @Test
    fun `Should warn when TSG homozygous disruption implies gain of function`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withHomozygousDisruption(matchingHomDisruption.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION))
            )
        )
    }

    @Test
    fun `Should pass with matching TSG loss`() {
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withCopyNumber(matchingLoss)))
    }

    @Test
    fun `Should warn when TSG loss is not reportable`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(MolecularTestFactory.withCopyNumber(matchingLoss.copy(isReportable = false)))
        )
    }

    @Test
    fun `Should warn when lost gene is an oncogene`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(MolecularTestFactory.withCopyNumber(matchingLoss.copy(geneRole = GeneRole.ONCO)))
        )
    }

    @Test
    fun `Should warn when lost gene implies gain of function`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withCopyNumber(matchingLoss.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION)))
        )
    }

    @Test
    fun `Should pass with matching TSG variant`() {
        assertResultForVariant(EvaluationResult.PASS, matchingVariant)
    }

    @Test
    fun `Should warn when TSG variant is not reportable`() {
        assertResultForVariant(EvaluationResult.WARN, matchingVariant.copy(isReportable = false))
    }

    @Test
    fun `Should warn when variant affects oncogene`() {
        assertResultForVariant(EvaluationResult.WARN, matchingVariant.copy(geneRole = GeneRole.ONCO))
    }

    @Test
    fun `Should warn when TSG variant implies gain of function`() {
        assertResultForVariant(EvaluationResult.WARN, matchingVariant.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION))
    }

    @Test
    fun `Should warn when TSG variant has no high driver likelihood`() {
        assertResultForVariant(EvaluationResult.WARN, matchingVariant.copy(driverLikelihood = DriverLikelihood.MEDIUM))
    }

    @Test
    fun `Should warn when TSG variant is not biallelic`() {
        assertResultForVariant(EvaluationResult.WARN, matchingVariant.copy(isBiallelic = false))
    }

    @Test
    fun `Should warn when TSG variant is subclonal`() {
        assertResultForVariant(EvaluationResult.WARN, matchingVariant.copy(clonalLikelihood = 0.4))
    }

    @Test
    fun `Should fail when TSG variant has no coding impact`() {
        assertResultForVariant(
            EvaluationResult.FAIL, matchingVariant.copy(
                canonicalImpact = TestTranscriptImpactFactory.createMinimal().copy(codingEffect = CodingEffect.NONE)
            )
        )
    }

    @Test
    fun `Should pass when TSG variant in high TML sample`() {
        assertResultForMutationalLoadAndVariant(EvaluationResult.PASS, true, matchingVariant)
    }

    @Test
    fun `Should fail when TSG variant has no high driver likelihood in high TML sample`() {
        assertResultForMutationalLoadAndVariant(
            EvaluationResult.FAIL, true, matchingVariant.copy(driverLikelihood = DriverLikelihood.LOW)
        )
    }

    @Test
    fun `Should warn when TSG variant is non biallelic and non high driver in low TML sample`() {
        assertResultForMutationalLoadAndVariant(
            EvaluationResult.WARN, false, matchingVariant.copy(driverLikelihood = DriverLikelihood.LOW, isBiallelic = false)
        )

    }

    @Test
    fun `Should warn when TSG variant is non high driver but biallelic in low TML sample`() {
        assertResultForMutationalLoadAndVariant(
            EvaluationResult.WARN, false, matchingVariant.copy(driverLikelihood = DriverLikelihood.LOW)
        )
    }

    @Test
    fun `Should fail with multiple low driver variants with overlapping phase groups and inactivating effects`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariants(
                    true, variantWithPhaseGroups(setOf(1)), variantWithPhaseGroups(setOf(1, 2))
                )
            )
        )
    }

    @Test
    fun `Should warn with multiple low driver variants with non overlapping phase groups and inactivating effects`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariants(
                    true, variantWithPhaseGroups(setOf(1)), variantWithPhaseGroups(setOf(2))
                )
            )
        )
    }

    @Test
    fun `Should warn with multiple low driver variants with unknown phase groups and inactivating effects`() {
        val variant1 = variantWithPhaseGroups(null)
        // Add copy number to make distinct:
        val variant2 = variant1.copy(variantCopyNumber = 1.0)

        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariants(true, variant1, variant2)
            )
        )
    }

    @Test
    fun `Should warn with low driver variant with inactivating effect and low driver disruption`() {
        val disruption = TestDisruptionFactory.createMinimal().copy(
            gene = GENE, isReportable = true, clusterGroup = 1, driverLikelihood = DriverLikelihood.LOW
        )
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
        result: EvaluationResult, hasHighTumorMutationalLoad: Boolean, variant: Variant
    ) {
        assertMolecularEvaluation(
            result, function.evaluate(MolecularTestFactory.withHasTumorMutationalLoadAndVariants(hasHighTumorMutationalLoad, variant))
        )
    }

    private fun variantWithPhaseGroups(phaseGroups: Set<Int>?): Variant = TestVariantFactory.createMinimal().copy(
        gene = GENE,
        isReportable = true,
        canonicalImpact = TestTranscriptImpactFactory.createMinimal().copy(codingEffect = CodingEffect.NONSENSE_OR_FRAMESHIFT),
        driverLikelihood = DriverLikelihood.LOW,
        phaseGroups = phaseGroups
    )
}