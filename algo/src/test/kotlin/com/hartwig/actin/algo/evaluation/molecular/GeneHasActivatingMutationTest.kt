package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptVariantImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.Variant
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GeneHasActivatingMutationTest {
    private val functionNotIgnoringCodons = GeneHasActivatingMutation(GENE, null)
    private val functionWithCodonsToIgnore = GeneHasActivatingMutation(GENE, CODONS_TO_IGNORE)

    @Test
    fun `Should fail for minimal patient`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            functionNotIgnoringCodons.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            functionWithCodonsToIgnore.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }

    @Test
    fun `Should pass with activating mutation for gene`() {
        assertResultForVariant(EvaluationResult.PASS, ACTIVATING_VARIANT)
        assertResultForVariantIgnoringCodons(EvaluationResult.PASS, ACTIVATING_VARIANT)
    }

    @Test
    fun `Should warn with activating mutation for gene if kinase domain requirement is true`() {
        val function = GeneHasActivatingMutation(GENE, null, inKinaseDomain = true)
        val result = function.evaluate(MolecularTestFactory.withVariant(ACTIVATING_VARIANT))

        assertMolecularEvaluation(EvaluationResult.WARN, result)
        assertThat(result.warnMessagesStrings()).containsExactly("gene A activating mutation(s): event but undetermined if in kinase domain")
    }

    @Test
    fun `Should fail with activating mutation for other gene`() {
        assertResultForVariant(EvaluationResult.FAIL, ACTIVATING_VARIANT.copy(gene = "gene B"))
        assertResultForVariantIgnoringCodons(EvaluationResult.FAIL, ACTIVATING_VARIANT.copy(gene = "gene B"))
    }

    @Test
    fun `Should fail with activating mutation for correct gene but codon to ignore`() {
        assertResultForVariantIgnoringCodons(EvaluationResult.FAIL, ACTIVATING_VARIANT_WITH_CODON_TO_IGNORE)
    }

    @Test
    fun `Should pass with one variant to ignore and one variant not to ignore`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS, functionWithCodonsToIgnore.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariants(
                    true,
                    ACTIVATING_VARIANT,
                    ACTIVATING_VARIANT_WITH_CODON_TO_IGNORE
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS, functionWithCodonsToIgnore.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariants(
                    false,
                    ACTIVATING_VARIANT,
                    ACTIVATING_VARIANT_WITH_CODON_TO_IGNORE
                )
            )
        )
    }

    @Test
    fun `Should warn with activating mutation for TSG`() {
        assertResultForVariant(EvaluationResult.WARN, ACTIVATING_VARIANT.copy(geneRole = GeneRole.TSG))
        assertResultForVariantIgnoringCodons(EvaluationResult.WARN, ACTIVATING_VARIANT.copy(geneRole = GeneRole.TSG))
    }

    @Test
    fun `Should warn with activating mutation with drug resistance for gene`() {
        assertResultForVariant(EvaluationResult.WARN, ACTIVATING_VARIANT.copy(isAssociatedWithDrugResistance = true))
        assertResultForVariantIgnoringCodons(EvaluationResult.WARN, ACTIVATING_VARIANT.copy(isAssociatedWithDrugResistance = true))
    }

    @Test
    fun `Should warn with activating mutation for gene with no protein effect or cancer-associated variant`() {
        assertResultForVariant(
            EvaluationResult.WARN,
            ACTIVATING_VARIANT.copy(proteinEffect = ProteinEffect.UNKNOWN, isCancerAssociatedVariant = false)
        )
    }

    @Test
    fun `Should warn with activating mutation for gene with low driver likelihood and unknown protein effect and unknown TML`() {
        assertResultForVariantWithTML(
            EvaluationResult.WARN,
            ACTIVATING_VARIANT.copy(proteinEffect = ProteinEffect.UNKNOWN, driverLikelihood = DriverLikelihood.LOW),
            null
        )
    }

    @Test
    fun `Should warn with non reportable missense mutation for gene`() {
        assertResultForVariant(
            EvaluationResult.WARN,
            TestVariantFactory.createMinimal().copy(
                gene = GENE,
                isReportable = false,
                isCancerAssociatedVariant = false,
                canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(codingEffect = CodingEffect.MISSENSE)
            )
        )
    }

    @Test
    fun `Should warn with non reportable cancer-associated variant for gene`() {
        assertResultForVariant(
            EvaluationResult.WARN,
            TestVariantFactory.createMinimal().copy(gene = GENE, isReportable = false, isCancerAssociatedVariant = true)
        )
    }

    @Test
    fun `Should warn with high driver subclonal activating mutation for gene`() {
        assertResultForVariant(
            EvaluationResult.WARN,
            TestVariantFactory.createMinimal().copy(
                gene = GENE,
                isReportable = true,
                driverLikelihood = DriverLikelihood.HIGH,
                clonalLikelihood = 0.2
            ),
        )
    }

    @Test
    fun `Should warn with low driver subclonal activating mutation for gene and unknown TML`() {
        assertResultForVariantWithTML(
            EvaluationResult.WARN,
            TestVariantFactory.createMinimal().copy(
                gene = GENE,
                isReportable = true,
                driverLikelihood = DriverLikelihood.LOW,
                clonalLikelihood = 0.2
            ),
            null
        )
    }

    @Test
    fun `Should fail with low driver subclonal activating mutation for gene and high TML`() {
        assertResultForVariantWithTML(
            EvaluationResult.FAIL,
            TestVariantFactory.createMinimal().copy(
                gene = GENE,
                isReportable = true,
                driverLikelihood = DriverLikelihood.LOW,
                clonalLikelihood = 0.2
            ),
            true
        )
    }

    @Test
    fun `Should pass with high driver activating mutation with high TML`() {
        assertResultForVariant(EvaluationResult.PASS, ACTIVATING_VARIANT)
    }

    @Test
    fun `Should fail with low driver activating mutation with high TML and unknown protein effect`() {
        assertResultForVariantWithTML(
            EvaluationResult.FAIL,
            ACTIVATING_VARIANT.copy(proteinEffect = ProteinEffect.UNKNOWN, driverLikelihood = DriverLikelihood.LOW),
            true
        )
    }

    @Test
    fun `Should warn with low driver activating mutation with low TML and unknown protein effect`() {
        assertResultForVariantWithTML(
            EvaluationResult.WARN,
            ACTIVATING_VARIANT.copy(proteinEffect = ProteinEffect.UNKNOWN, driverLikelihood = DriverLikelihood.LOW),
            false
        )
    }

    @Test
    fun `Should evaluate to undetermined when no molecular input`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            functionNotIgnoringCodons.evaluate(
                TestPatientFactory.createMinimalTestWGSPatientRecord().copy(molecularHistory = MolecularHistory.empty())
            )
        )
    }

    @Test
    fun `Should evaluate undetermined with appropriate message when target coverage insufficient`() {
        val result = functionNotIgnoringCodons.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
                molecularHistory = MolecularHistory(molecularTests = listOf(TestMolecularFactory.createMinimalTestPanelRecord()))
            )
        )
        assertThat(result.result).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(result.undeterminedMessagesStrings()).containsExactly("Activating mutation in gene gene A undetermined (not tested for mutations)")
    }

    private fun assertResultForVariant(expectedResult: EvaluationResult, variant: Variant) {
        assertResultForVariantWithTML(expectedResult, variant, null)

        // Repeat with high TML since unknown TML always results in a warning for reportable variants:
        assertResultForVariantWithTML(expectedResult, variant, true)

        if (expectedResult == EvaluationResult.WARN) {
            assertResultForVariantIgnoringCodons(expectedResult, variant)
        }
    }

    private fun assertResultForVariantIgnoringCodons(expectedResult: EvaluationResult, variant: Variant) {
        assertResultForVariantWithTMLIgnoringCodons(expectedResult, variant, null)
        assertResultForVariantWithTMLIgnoringCodons(expectedResult, variant, true)
    }

    private fun assertResultForVariantWithTML(expectedResult: EvaluationResult, variant: Variant, hasHighTML: Boolean?) {
        assertMolecularEvaluation(
            expectedResult,
            functionNotIgnoringCodons.evaluate(MolecularTestFactory.withHasTumorMutationalLoadAndVariants(hasHighTML, variant))
        )
        if (expectedResult == EvaluationResult.WARN) {
            assertResultForVariantWithTMLIgnoringCodons(expectedResult, variant, hasHighTML)
        }
    }

    private fun assertResultForVariantWithTMLIgnoringCodons(
        expectedResult: EvaluationResult,
        variant: Variant,
        hasHighTML: Boolean?
    ) {
        assertMolecularEvaluation(
            expectedResult,
            functionWithCodonsToIgnore.evaluate(MolecularTestFactory.withHasTumorMutationalLoadAndVariants(hasHighTML, variant))
        )
    }

    companion object {
        private const val GENE = "gene A"
        private val CODONS_TO_IGNORE = listOf("A100X", "A200X")
        private val ACTIVATING_VARIANT = TestVariantFactory.createMinimal().copy(
            gene = GENE,
            event = "event",
            isReportable = true,
            driverLikelihood = DriverLikelihood.HIGH,
            geneRole = GeneRole.ONCO,
            proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
            isCancerAssociatedVariant = true,
            isAssociatedWithDrugResistance = false,
            canonicalImpact = impactWithCodon(300),
            clonalLikelihood = 0.8
        )

        private val ACTIVATING_VARIANT_WITH_CODON_TO_IGNORE = TestVariantFactory.createMinimal().copy(
            gene = GENE,
            isReportable = true,
            driverLikelihood = DriverLikelihood.HIGH,
            geneRole = GeneRole.ONCO,
            proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
            isCancerAssociatedVariant = true,
            isAssociatedWithDrugResistance = false,
            canonicalImpact = impactWithCodon(100),
            clonalLikelihood = 0.8
        )

        private fun impactWithCodon(affectedCodon: Int) =
            TestTranscriptVariantImpactFactory.createMinimal().copy(affectedCodon = affectedCodon)
    }
}