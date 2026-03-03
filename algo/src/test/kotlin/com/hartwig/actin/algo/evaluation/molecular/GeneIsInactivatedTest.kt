package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory.withHomologousRecombinationAndVariant
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory.withMicrosatelliteStabilityAndVariant
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IhcTest
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
import com.hartwig.actin.molecular.util.GeneConstants
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val GENE = "gene A"
private const val IHC_EVALUABLE_GENE = "MSH2"

class GeneIsInactivatedTest {

    private val functionInactivation = GeneIsInactivated(gene = GENE, onlyDeletions = false)
    private val functionDeletion = GeneIsInactivated(gene = GENE, onlyDeletions = true)

    private val matchingIhcResult = IhcTest(item = IHC_EVALUABLE_GENE, scoreText = "Loss")

    private val matchingHomDisruption = TestHomozygousDisruptionFactory.createMinimal().copy(
        gene = GENE, isReportable = true, geneRole = GeneRole.TSG, proteinEffect = ProteinEffect.LOSS_OF_FUNCTION, event = "event"
    )

    private val matchingDel = TestCopyNumberFactory.createMinimal().copy(
        gene = GENE,
        isReportable = true,
        geneRole = GeneRole.TSG,
        proteinEffect = ProteinEffect.LOSS_OF_FUNCTION,
        canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_DEL)
    )

    private val matchingVariant = TestVariantFactory.createMinimal().copy(
        gene = GENE,
        isReportable = true,
        driverLikelihood = DriverLikelihood.HIGH,
        isBiallelic = true,
        clonalLikelihood = 1.0,
        geneRole = GeneRole.TSG,
        proteinEffect = ProteinEffect.LOSS_OF_FUNCTION,
        canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(
            codingEffect = GeneIsInactivated.INACTIVATING_CODING_EFFECTS.first()
        )
    )
    private val nonHighDriverNonBiallelicMatchingVariant = matchingVariant.copy(
        driverLikelihood = DriverLikelihood.LOW,
        isBiallelic = false
    )

    @Test
    fun `Should fail without any alterations for both functions`() {
        assertBothFunctions(EvaluationResult.FAIL, TestPatientFactory.createMinimalTestWGSPatientRecord())
    }

    @Test
    fun `Should select right fail message without any alterations for both functions`() {
        val evaluationInactivation = functionInactivation.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        val evaluationDeletion = functionDeletion.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())

        assertThat(evaluationInactivation.failMessagesStrings()).containsExactly("No gene A inactivation")
        assertThat(evaluationDeletion.failMessagesStrings()).containsExactly("No gene A deletion")
    }

    @Test
    fun `Should pass with matching TSG homozygous disruption for both functions`() {
        assertBothFunctions(EvaluationResult.PASS, MolecularTestFactory.withHomozygousDisruption(matchingHomDisruption))
    }

    @Test
    fun `Should select right pass message with passing event`() {
        val evaluationInactivation = functionInactivation.evaluate(MolecularTestFactory.withHomozygousDisruption(matchingHomDisruption))
        val evaluationDeletion = functionDeletion.evaluate(MolecularTestFactory.withHomozygousDisruption(matchingHomDisruption))

        assertThat(evaluationInactivation.passMessagesStrings()).containsExactly("gene A inactivation (event)")
        assertThat(evaluationDeletion.passMessagesStrings()).containsExactly("gene A deletion (event)")
    }

    @Test
    fun `Should warn when TSG homozygous disruption is not reportable for both functions`() {
        assertBothFunctions(
            EvaluationResult.WARN,
            MolecularTestFactory.withHomozygousDisruption(matchingHomDisruption.copy(isReportable = false))
        )
    }

    @Test
    fun `Should select right warn message with passing event`() {
        val evaluationInactivation =
            functionInactivation.evaluate(MolecularTestFactory.withHomozygousDisruption(matchingHomDisruption.copy(isReportable = false)))
        val evaluationDeletion =
            functionDeletion.evaluate(MolecularTestFactory.withHomozygousDisruption(matchingHomDisruption.copy(isReportable = false)))

        assertThat(evaluationInactivation.warnMessagesStrings()).containsExactly("Inactivation event(s) event for gene A but event(s) not reportable")
        assertThat(evaluationDeletion.warnMessagesStrings()).containsExactly("Deletion event(s) event for gene A but event(s) not reportable")
    }

    @Test
    fun `Should warn when homozygously disrupted gene is an oncogene`() {
        assertBothFunctions(
            EvaluationResult.WARN,
            MolecularTestFactory.withHomozygousDisruption(matchingHomDisruption.copy(geneRole = GeneRole.ONCO))
        )
    }

    @Test
    fun `Should warn when TSG homozygous disruption implies gain of function`() {
        assertBothFunctions(
            EvaluationResult.WARN,
            MolecularTestFactory.withHomozygousDisruption(matchingHomDisruption.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION))
        )
    }

    @Test
    fun `Should warn when TSG homozygous disruption implies no protein effect`() {
        assertBothFunctions(
            EvaluationResult.WARN,
            MolecularTestFactory.withHomozygousDisruption(matchingHomDisruption.copy(proteinEffect = ProteinEffect.NO_EFFECT))
        )
    }

    @Test
    fun `Should pass with matching TSG deletion`() {
        assertBothFunctions(EvaluationResult.PASS, MolecularTestFactory.withCopyNumber(matchingDel))
    }

    @Test
    fun `Should warn when TSG deletion is not reportable`() {
        assertBothFunctions(
            EvaluationResult.WARN, MolecularTestFactory.withCopyNumber(matchingDel.copy(isReportable = false))
        )
    }

    @Test
    fun `Should warn when lost gene is an oncogene`() {
        assertBothFunctions(
            EvaluationResult.WARN,
            MolecularTestFactory.withCopyNumber(matchingDel.copy(geneRole = GeneRole.ONCO))
        )
    }

    @Test
    fun `Should warn when lost gene implies gain of function`() {
        assertBothFunctions(
            EvaluationResult.WARN,
            MolecularTestFactory.withCopyNumber(matchingDel.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION))
        )
    }

    @Test
    fun `Should warn when lost gene implies no protein effect`() {
        assertBothFunctions(
            EvaluationResult.WARN,
            MolecularTestFactory.withCopyNumber(matchingDel.copy(proteinEffect = ProteinEffect.NO_EFFECT))
        )
    }

    @Test
    fun `Should pass with matching IHC event if looking for inactivation`() {
        val result = GeneIsInactivated(
            gene = IHC_EVALUABLE_GENE,
            onlyDeletions = false
        ).evaluate(MolecularTestFactory.withIhcTests(matchingIhcResult))

        assertMolecularEvaluation(EvaluationResult.PASS, result)
        assertThat(result.passMessagesStrings()).containsExactly("MSH2 inactivation (MSH2 loss by IHC)")
    }

    @Test
    fun `Should warn with matching IHC event if looking for only deletions`() {
        val result = GeneIsInactivated(
            gene = IHC_EVALUABLE_GENE,
            onlyDeletions = true
        ).evaluate(MolecularTestFactory.withIhcTests(matchingIhcResult))

        assertMolecularEvaluation(EvaluationResult.WARN, result)
        assertThat(result.warnMessagesStrings()).containsExactly("MSH2 loss by IHC may indicate MSH2 gene deletion")
    }

    @Test
    fun `Should warn with potentially eligible IHC event for both functions`() {
        assertBothFunctionsForIhc(
            EvaluationResult.WARN,
            MolecularTestFactory.withIhcTests(matchingIhcResult.copy(scoreText = "possible loss"))
        )
    }

    @Test
    fun `Should fail with potentially eligible IHC event for both functions if correct result but on wrong gene`() {
        assertBothFunctionsForIhc(
            EvaluationResult.FAIL,
            MolecularTestFactory.withIhcTests(matchingIhcResult.copy(item = GENE))
        )
    }

    @Test
    fun `Should fail with matching IHC event for both functions if gene is not considered for IHC`() {
        assertBothFunctions(EvaluationResult.FAIL, MolecularTestFactory.withIhcTests(matchingIhcResult.copy(item = GENE)))
    }

    @Test
    fun `Should pass with matching TSG variant when requesting inactivation`() {
        assertResultForVariantForInactivation(EvaluationResult.PASS, matchingVariant)
    }

    @Test
    fun `Should fail with matching TSG variant when requesting deletions only`() {
        assertResultForVariantForDeletion(EvaluationResult.FAIL, matchingVariant)
    }

    @Test
    fun `Should warn with matching TSG variant when unknown if biallelic`() {
        assertResultForVariantForInactivation(EvaluationResult.WARN, matchingVariant.copy(isBiallelic = null))
    }

    @Test
    fun `Should fail with matching TSG variant when unknown if biallelic when requesting deletions only`() {
        assertResultForVariantForDeletion(EvaluationResult.FAIL, matchingVariant.copy(isBiallelic = null))
    }

    @Test
    fun `Should pass with matching TSG variant when unknown if clonal`() {
        assertResultForVariantForInactivation(
            EvaluationResult.PASS,
            matchingVariant.copy(clonalLikelihood = null)
        )
    }

    @Test
    fun `Should fail with matching TSG variant but not clonal`() {
        assertResultForVariantForInactivation(
            EvaluationResult.FAIL,
            matchingVariant.copy(clonalLikelihood = 0.4)
        )
    }

    @Test
    fun `Should warn when TSG variant is not reportable`() {
        assertResultForVariantForInactivation(EvaluationResult.WARN, matchingVariant.copy(isReportable = false))
    }

    @Test
    fun `Should warn when variant affects oncogene`() {
        assertResultForVariantForInactivation(EvaluationResult.WARN, matchingVariant.copy(geneRole = GeneRole.ONCO))
    }

    @Test
    fun `Should warn when TSG variant implies gain of function`() {
        assertResultForVariantForInactivation(EvaluationResult.WARN, matchingVariant.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION))
    }

    @Test
    fun `Should warn when TSG variant implies no protein effect`() {
        assertResultForVariantForInactivation(EvaluationResult.WARN, matchingVariant.copy(proteinEffect = ProteinEffect.NO_EFFECT))
    }

    @Test
    fun `Should warn when TSG variant has no high driver likelihood`() {
        assertResultForVariantForInactivation(EvaluationResult.WARN, matchingVariant.copy(driverLikelihood = DriverLikelihood.MEDIUM))
    }

    @Test
    fun `Should warn when TSG variant is not biallelic`() {
        assertResultForVariantForInactivation(
            EvaluationResult.WARN,
            matchingVariant.copy(isBiallelic = false)
        )
    }

    @Test
    fun `Should warn when deletion is only on non-canonical transcript`() {
        assertBothFunctions(
            EvaluationResult.WARN,
            MolecularTestFactory.withCopyNumber(
                matchingDel.copy(
                    canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(),
                    otherImpacts = setOf(TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.PARTIAL_DEL))
                )
            )
        )
    }

    @Test
    fun `Should fail when TSG variant has no coding impact`() {
        assertResultForVariantForInactivation(
            EvaluationResult.FAIL, matchingVariant.copy(
                canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(codingEffect = CodingEffect.NONE)
            )
        )
    }

    @Test
    fun `Should pass when TSG variant in high TML sample`() {
        assertResultForMutationalLoadAndVariantForInactivation(EvaluationResult.PASS, true, matchingVariant)
    }

    @Test
    fun `Should fail when TSG variant is non biallelic and non high driver`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            functionInactivation.evaluate(MolecularTestFactory.withVariant(nonHighDriverNonBiallelicMatchingVariant))
        )
    }

    @Test
    fun `Should fail when TSG variant has no high driver likelihood in high TML sample`() {
        assertResultForMutationalLoadAndVariantForInactivation(
            EvaluationResult.FAIL, true, matchingVariant.copy(driverLikelihood = DriverLikelihood.LOW)
        )
    }

    @Test
    fun `Should warn when TSG variant has no high driver likelihood in low TML sample`() {
        assertResultForMutationalLoadAndVariantForInactivation(
            EvaluationResult.WARN, false, matchingVariant.copy(driverLikelihood = DriverLikelihood.LOW)
        )
    }

    @Test
    fun `Should warn when TSG variant is non biallelic and non high driver in MSI gene in MSI sample`() {
        val mmrGene = GeneConstants.MMR_GENES.first()
        val function = GeneIsInactivated(mmrGene, onlyDeletions = false)
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                withMicrosatelliteStabilityAndVariant(true, nonHighDriverNonBiallelicMatchingVariant.copy(gene = mmrGene))
            )
        )
    }

    @Test
    fun `Should fail when TSG variant is non biallelic and non high driver in MSI gene in MSI sample when requesting only deletions`() {
        val mmrGene = GeneConstants.MMR_GENES.first()
        val function = GeneIsInactivated(mmrGene, onlyDeletions = true)
        assertMolecularEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                withMicrosatelliteStabilityAndVariant(true, nonHighDriverNonBiallelicMatchingVariant.copy(gene = mmrGene))
            )
        )
    }

    @Test
    fun `Should fail when TSG variant is non biallelic and non high driver in MSI gene in MS-Stable sample`() {
        val mmrGene = GeneConstants.MMR_GENES.first()
        val function = GeneIsInactivated(mmrGene, onlyDeletions = false)
        assertMolecularEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                withMicrosatelliteStabilityAndVariant(false, nonHighDriverNonBiallelicMatchingVariant.copy(gene = mmrGene))
            )
        )
    }

    @Test
    fun `Should warn when TSG variant is non biallelic and non high driver in HRD gene in HRD sample`() {
        val hrGene = GeneConstants.HR_GENES.first()
        val function = GeneIsInactivated(hrGene, onlyDeletions = false)
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                withHomologousRecombinationAndVariant(true, nonHighDriverNonBiallelicMatchingVariant.copy(gene = hrGene))
            )
        )
    }

    @Test
    fun `Should fail when TSG variant is non biallelic and non high driver in HRD gene in HR-Proficient sample`() {
        val hrGene = GeneConstants.HR_GENES.first()
        val function = GeneIsInactivated(hrGene, onlyDeletions = false)
        assertMolecularEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                withHomologousRecombinationAndVariant(false, nonHighDriverNonBiallelicMatchingVariant.copy(gene = hrGene))
            )
        )
    }

    @Test
    fun `Should warn when TSG variant is non high driver but biallelic in low TML sample`() {
        assertResultForMutationalLoadAndVariantForInactivation(
            EvaluationResult.WARN, false, matchingVariant.copy(driverLikelihood = DriverLikelihood.LOW)
        )
    }

    @Test
    fun `Should fail with multiple low driver variants with overlapping phase groups and inactivating effects`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL, functionInactivation.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariants(
                    true, variantWithPhaseGroups(setOf(1)), variantWithPhaseGroups(setOf(1, 2))
                )
            )
        )
    }

    @Test
    fun `Should warn with multiple low driver variants with non overlapping phase groups and inactivating effects`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, functionInactivation.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariants(
                    true, variantWithPhaseGroups(setOf(1)), variantWithPhaseGroups(setOf(2))
                )
            )
        )
    }

    @Test
    fun `Should warn with multiple low driver variants with unknown phase groups and inactivating effects`() {
        val variant1 = variantWithPhaseGroups(null)
        val variant2 = variant1.copy(variantCopyNumber = 1.0)

        assertMolecularEvaluation(
            EvaluationResult.WARN, functionInactivation.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariants(true, variant1, variant2)
            )
        )
    }

    @Test
    fun `Should warn with low driver variant with inactivating effect and low driver disruption when requesting inactivation`() {
        val disruption = TestDisruptionFactory.createMinimal().copy(
            gene = GENE, isReportable = true, clusterGroup = 1, driverLikelihood = DriverLikelihood.LOW
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN, functionInactivation.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariantAndDisruption(
                    true, variantWithPhaseGroups(setOf(1)), disruption
                )
            )
        )
    }

    @Test
    fun `Should fail with low driver variant with inactivating effect and low driver disruption when requesting only deletions`() {
        val disruption = TestDisruptionFactory.createMinimal().copy(
            gene = GENE, isReportable = true, clusterGroup = 1, driverLikelihood = DriverLikelihood.LOW
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL, functionDeletion.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariantAndDisruption(
                    true, variantWithPhaseGroups(setOf(1)), disruption
                )
            )
        )
    }

    @Test
    fun `Should evaluate undetermined with appropriate message when target coverage insufficient when requesting inactivation`() {
        val result = functionInactivation.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
                molecularTests = listOf(TestMolecularFactory.createMinimalPanelTest())
            )
        )
        Assertions.assertThat(result.result).isEqualTo(EvaluationResult.UNDETERMINED)
        Assertions.assertThat(result.undeterminedMessagesStrings())
            .containsExactly("Inactivation of gene gene A undetermined (not tested for mutations or deletions)")
    }

    @Test
    fun `Should evaluate undetermined with appropriate message when target coverage insufficient when requesting only deletions`() {
        val result = functionDeletion.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
                molecularTests = listOf(TestMolecularFactory.createMinimalPanelTest())
            )
        )
        Assertions.assertThat(result.result).isEqualTo(EvaluationResult.UNDETERMINED)
        Assertions.assertThat(result.undeterminedMessagesStrings())
            .containsExactly("Deletion of gene gene A undetermined (not tested for deletions)")
    }

    private fun assertResultForVariantForDeletion(result: EvaluationResult, variant: Variant) {
        assertMolecularEvaluation(result, functionDeletion.evaluate(MolecularTestFactory.withVariant(variant)))
    }

    private fun assertResultForVariantForInactivation(result: EvaluationResult, variant: Variant) {
        assertMolecularEvaluation(result, functionInactivation.evaluate(MolecularTestFactory.withVariant(variant)))
    }

    private fun assertResultForMutationalLoadAndVariantForInactivation(
        result: EvaluationResult, hasHighTumorMutationalLoad: Boolean, variant: Variant
    ) {
        assertMolecularEvaluation(
            result,
            functionInactivation.evaluate(MolecularTestFactory.withHasTumorMutationalLoadAndVariants(hasHighTumorMutationalLoad, variant))
        )
    }

    private fun assertBothFunctions(result: EvaluationResult, record: PatientRecord) {
        assertMolecularEvaluation(result, functionInactivation.evaluate(record))
        assertMolecularEvaluation(result, functionDeletion.evaluate(record))
    }

    private fun assertBothFunctionsForIhc(result: EvaluationResult, record: PatientRecord) {
        assertMolecularEvaluation(result, GeneIsInactivated(gene = IHC_EVALUABLE_GENE, onlyDeletions = false).evaluate(record))
        assertMolecularEvaluation(result, GeneIsInactivated(gene = IHC_EVALUABLE_GENE, onlyDeletions = true).evaluate(record))
    }

    private fun variantWithPhaseGroups(phaseGroups: Set<Int>?) = TestVariantFactory.createMinimal().copy(
        gene = GENE,
        isReportable = true,
        canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(codingEffect = CodingEffect.NONSENSE_OR_FRAMESHIFT),
        driverLikelihood = DriverLikelihood.LOW,
        phaseGroups = phaseGroups
    )
}