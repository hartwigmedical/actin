package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory.withHomologousRecombinationAndVariant
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory.withMicrosatelliteStabilityAndVariant
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptVariantImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.Variant
import org.assertj.core.api.Assertions
import org.junit.Test

private const val GENE = "gene A"

class GeneIsInactivatedTest {

    private val function = GeneIsInactivated(GENE)

    private val matchingHomDisruption = TestHomozygousDisruptionFactory.createMinimal().copy(
        gene = GENE, isReportable = true, geneRole = GeneRole.TSG, proteinEffect = ProteinEffect.LOSS_OF_FUNCTION
    )

    private val matchingDel = TestCopyNumberFactory.createMinimal().copy(
        gene = GENE,
        isReportable = true,
        geneRole = GeneRole.TSG,
        proteinEffect = ProteinEffect.LOSS_OF_FUNCTION,
        canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.DEL)
    )

    private val matchingVariant = TestVariantFactory.createMinimal().copy(
        gene = GENE,
        isReportable = true,
        driverLikelihood = DriverLikelihood.HIGH,
        extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(isBiallelic = true),
        geneRole = GeneRole.TSG,
        proteinEffect = ProteinEffect.LOSS_OF_FUNCTION,
        canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(
            codingEffect = GeneIsInactivated.INACTIVATING_CODING_EFFECTS.first()
        )
    )
    private val nonHighDriverNonBiallelicMatchingVariant = matchingVariant.copy(
        driverLikelihood = DriverLikelihood.LOW,
        extendedVariantDetails = matchingVariant.extendedVariantDetails?.copy(isBiallelic = false),
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
    fun `Should pass with matching TSG deletion`() {
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withCopyNumber(matchingDel)))
    }

    @Test
    fun `Should warn when TSG deletion is not reportable`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(MolecularTestFactory.withCopyNumber(matchingDel.copy(isReportable = false)))
        )
    }

    @Test
    fun `Should warn when lost gene is an oncogene`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(MolecularTestFactory.withCopyNumber(matchingDel.copy(geneRole = GeneRole.ONCO)))
        )
    }

    @Test
    fun `Should warn when lost gene implies gain of function`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withCopyNumber(matchingDel.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION)))
        )
    }

    @Test
    fun `Should pass with matching TSG variant`() {
        assertResultForVariant(EvaluationResult.PASS, matchingVariant)
    }

    @Test
    fun `Should pass with matching TSG variant when unknown if clonal`() {
        assertResultForVariant(
            EvaluationResult.PASS,
            matchingVariant.copy(extendedVariantDetails = null)
        )
    }

    @Test
    fun `Should fail with matching TSG variant but not clonal`() {
        assertResultForVariant(
            EvaluationResult.FAIL,
            matchingVariant.copy(extendedVariantDetails = matchingVariant.extendedVariantDetails?.copy(clonalLikelihood = 0.4))
        )
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
        assertResultForVariant(
            EvaluationResult.WARN,
            matchingVariant.copy(extendedVariantDetails = matchingVariant.extendedVariantDetails?.copy(isBiallelic = false))
        )
    }

    @Test
    fun `Should warn when deletion is only on non-canonical transcript`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withCopyNumber(
                    matchingDel.copy(
                        canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(),
                        otherImpacts = setOf(TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.DEL))
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail when TSG variant has no coding impact`() {
        assertResultForVariant(
            EvaluationResult.FAIL, matchingVariant.copy(
                canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(codingEffect = CodingEffect.NONE)
            )
        )
    }

    @Test
    fun `Should pass when TSG variant in high TML sample`() {
        assertResultForMutationalLoadAndVariant(EvaluationResult.PASS, true, matchingVariant)
    }

    @Test
    fun `Should fail when TSG variant is non biallelic and non high driver`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withVariant(nonHighDriverNonBiallelicMatchingVariant))
        )
    }

    @Test
    fun `Should fail when TSG variant has no high driver likelihood in high TML sample`() {
        assertResultForMutationalLoadAndVariant(
            EvaluationResult.FAIL, true, matchingVariant.copy(driverLikelihood = DriverLikelihood.LOW)
        )
    }

    @Test
    fun `Should warn when TSG variant has no high driver likelihood in low TML sample`() {
        assertResultForMutationalLoadAndVariant(
            EvaluationResult.WARN, false, matchingVariant.copy(driverLikelihood = DriverLikelihood.LOW)
        )
    }

    @Test
    fun `Should warn when TSG variant is non biallelic and non high driver in MSI gene in MSI sample`() {
        val msiGene = MolecularConstants.MSI_GENES.iterator().next()
        val function = GeneIsInactivated(msiGene)
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                withMicrosatelliteStabilityAndVariant(true, nonHighDriverNonBiallelicMatchingVariant.copy(gene = msiGene))
            )
        )
    }

    @Test
    fun `Should fail when TSG variant is non biallelic and non high driver in MSI gene in MS-Stable sample`() {
        val msiGene = MolecularConstants.MSI_GENES.iterator().next()
        val function = GeneIsInactivated(msiGene)
        assertMolecularEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                withMicrosatelliteStabilityAndVariant(false, nonHighDriverNonBiallelicMatchingVariant.copy(gene = msiGene))
            )
        )
    }

    @Test
    fun `Should warn when TSG variant is non biallelic and non high driver in HRD gene in HRD sample`() {
        val hrdGene = MolecularConstants.HRD_GENES.iterator().next()
        val function = GeneIsInactivated(hrdGene)
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                withHomologousRecombinationAndVariant(true, nonHighDriverNonBiallelicMatchingVariant.copy(gene = hrdGene))
            )
        )
    }

    @Test
    fun `Should fail when TSG variant is non biallelic and non high driver in HRD gene in HR-Proficient sample`() {
        val hrdGene = MolecularConstants.HRD_GENES.iterator().next()
        val function = GeneIsInactivated(hrdGene)
        assertMolecularEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                withHomologousRecombinationAndVariant(false, nonHighDriverNonBiallelicMatchingVariant.copy(gene = hrdGene))
            )
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
        val variant2 = variant1.copy(extendedVariantDetails = variant1.extendedVariantDetails?.copy(variantCopyNumber = 1.0))

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

    @Test
    fun `Should evaluate undetermined with appropriate message when target coverage insufficient`() {
        val result = function.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
                molecularHistory = MolecularHistory(molecularTests = listOf(TestMolecularFactory.createMinimalTestPanelRecord()))
            )
        )
        Assertions.assertThat(result.result).isEqualTo(EvaluationResult.UNDETERMINED)
        Assertions.assertThat(result.undeterminedMessages)
            .containsExactly("Inactivation of gene gene A undetermined (not tested for mutations or deletions)")
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

    private fun variantWithPhaseGroups(phaseGroups: Set<Int>?) = TestVariantFactory.createMinimal().copy(
        gene = GENE,
        isReportable = true,
        canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(codingEffect = CodingEffect.NONSENSE_OR_FRAMESHIFT),
        driverLikelihood = DriverLikelihood.LOW,
        extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(phaseGroups = phaseGroups)
    )
}