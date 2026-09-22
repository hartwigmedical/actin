package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptVariantImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.datamodel.trial.VariantTypeInput
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GeneHasActivatingMutationTest {

    private val functionNotIgnoringCodons = GeneHasActivatingMutation(GENE, null)
    private val functionWithCodonsToIgnore = GeneHasActivatingMutation(GENE, CODONS_TO_IGNORE)

    @Test
    fun `Should fail for patient with minimal WGS record`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            functionNotIgnoringCodons.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()),
            "No gene A activating mutation(s)"
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            functionWithCodonsToIgnore.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()),
            "No gene A activating mutation(s)"
        )
    }

    @Test
    fun `Should pass with activating mutation for gene`() {
        assertResultForVariant(EvaluationResult.PASS, ACTIVATING_VARIANT, "gene A activating mutation(s): event")
        assertResultForVariantIgnoringCodons(EvaluationResult.PASS, ACTIVATING_VARIANT, "gene A activating mutation(s): event")
    }

    @Test
    fun `Should warn with activating mutation for gene if kinase domain requirement is true`() {
        val function = GeneHasActivatingMutation(GENE, null, inKinaseDomain = true)
        val result = function.evaluate(MolecularTestFactory.withVariant(ACTIVATING_VARIANT))

        assertMolecularEvaluation(
            EvaluationResult.WARN,
            result,
            "gene A activating mutation(s): event but undetermined if in kinase domain"
        )
    }

    @Test
    fun `Should fail with activating mutation for other gene`() {
        assertResultForVariant(EvaluationResult.FAIL, ACTIVATING_VARIANT.copy(gene = "gene B"), "No gene A activating mutation(s)")
        assertResultForVariantIgnoringCodons(
            EvaluationResult.FAIL,
            ACTIVATING_VARIANT.copy(gene = "gene B"),
            "No gene A activating mutation(s)"
        )
    }

    @Test
    fun `Should fail with activating mutation for correct gene but codon to ignore`() {
        assertResultForVariantIgnoringCodons(
            EvaluationResult.FAIL,
            ACTIVATING_VARIANT_WITH_CODON_TO_IGNORE,
            "No gene A activating mutation(s)"
        )
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
            ),
            "gene A activating mutation(s): event"
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS, functionWithCodonsToIgnore.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariants(
                    false,
                    ACTIVATING_VARIANT,
                    ACTIVATING_VARIANT_WITH_CODON_TO_IGNORE
                )
            ),
            "gene A activating mutation(s): event"
        )
    }

    @Test
    fun `Should warn with activating mutation for TSG`() {
        assertResultForVariant(
            EvaluationResult.WARN,
            ACTIVATING_VARIANT.copy(geneRole = GeneRole.TSG),
            "gene A activating mutation(s) event - however gene known as TSG in evidence source"
        )
        assertResultForVariantIgnoringCodons(
            EvaluationResult.WARN,
            ACTIVATING_VARIANT.copy(geneRole = GeneRole.TSG),
            "gene A activating mutation(s) event - however gene known as TSG in evidence source"
        )
    }

    @Test
    fun `Should warn with activating mutation for gene with no protein effect or cancer-associated variant`() {
        assertResultForVariant(
            EvaluationResult.WARN,
            ACTIVATING_VARIANT.copy(proteinEffect = ProteinEffect.UNKNOWN, isCancerAssociatedVariant = false),
            "gene A potentially activating mutation(s) event with high driver likelihood - however not a cancer-associated variant"
        )
    }

    @Test
    fun `Should warn with activating mutation for gene with low driver likelihood and unknown protein effect and unknown TML`() {
        assertResultForVariantWithTML(
            EvaluationResult.WARN,
            ACTIVATING_VARIANT.copy(proteinEffect = ProteinEffect.UNKNOWN, driverLikelihood = DriverLikelihood.LOW),
            null,
            "gene A potentially activating mutation(s) event but no high driver likelihood"
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
                canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(codingEffect = CodingEffect.MISSENSE),
                event = "event"
            ),
            "gene A potentially activating mutation(s) event that are missense or have cancer-associated variant status but are not considered reportable"
        )
    }

    @Test
    fun `Should warn with non reportable cancer-associated variant for gene`() {
        assertResultForVariant(
            EvaluationResult.WARN,
            TestVariantFactory.createMinimal().copy(gene = GENE, isReportable = false, isCancerAssociatedVariant = true, event = "event"),
            "gene A potentially activating mutation(s) event that are missense or have cancer-associated variant status but are not considered reportable"
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
                clonalLikelihood = 0.2,
                event = "event"
            ),
            "gene A potentially activating mutation(s) event with high driver likelihood - however not a cancer-associated variant"
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
                clonalLikelihood = 0.2,
                event = "event"
            ),
            null,
            "gene A potentially activating mutation(s) event have subclonal likelihood of > 50% and no high driver likelihood"
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
            true,
            "No gene A activating mutation(s)"
        )
    }

    @Test
    fun `Should pass with high driver activating mutation with high TML`() {
        assertResultForVariant(EvaluationResult.PASS, ACTIVATING_VARIANT, "gene A activating mutation(s): event")
    }

    @Test
    fun `Should fail with low driver activating mutation with high TML and unknown protein effect`() {
        assertResultForVariantWithTML(
            EvaluationResult.FAIL,
            ACTIVATING_VARIANT.copy(proteinEffect = ProteinEffect.UNKNOWN, driverLikelihood = DriverLikelihood.LOW),
            true,
            "No gene A activating mutation(s)"
        )
    }

    @Test
    fun `Should warn with low driver activating mutation with low TML and unknown protein effect`() {
        assertResultForVariantWithTML(
            EvaluationResult.WARN,
            ACTIVATING_VARIANT.copy(proteinEffect = ProteinEffect.UNKNOWN, driverLikelihood = DriverLikelihood.LOW),
            false,
            "gene A potentially activating mutation(s) event but no high driver likelihood"
        )
    }

    @Test
    fun `Should evaluate to undetermined when no molecular input`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            functionNotIgnoringCodons.evaluate(
                TestPatientFactory.createMinimalTestWGSPatientRecord().copy(molecularTests = emptyList())
            ),
            "No molecular results of sufficient quality"
        )
    }

    @Test
    fun `Should evaluate undetermined with appropriate message when target coverage insufficient`() {
        val result = functionNotIgnoringCodons.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
                molecularTests = listOf(TestMolecularFactory.createMinimalPanelTest())
            )
        )
        assertThat(result.result).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(result.undeterminedMessagesStrings())
            .containsExactly("Activating mutation in gene gene A undetermined (not tested for mutations)")
    }

    @Test
    fun `Should fail with activating mutation for correct gene but protein impact to ignore`() {
        val function = GeneHasActivatingMutation(GENE, codonsToIgnore = null, proteinImpactsToIgnore = setOf("V600E"))
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    ACTIVATING_VARIANT.copy(
                        canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal()
                            .copy(hgvsProteinImpact = "p.V600E", affectedCodon = 300)
                    )
                )
            ),
            "No gene A activating mutation(s)"
        )
    }

    @Test
    fun `Should pass with activating mutation for correct gene when protein impact differs from ignored`() {
        val function = GeneHasActivatingMutation(GENE, codonsToIgnore = null, proteinImpactsToIgnore = setOf("V600E"))
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(MolecularTestFactory.withVariant(ACTIVATING_VARIANT)),
            "gene A activating mutation(s): event"
        )
    }

    @Test
    fun `Should fail with activating mutation for correct gene but exon and type to ignore`() {
        val function = GeneHasActivatingMutation(
            GENE, codonsToIgnore = null, variantTypeToIgnore = VariantTypeInput.DELETE, exonToIgnore = 2
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    ACTIVATING_VARIANT.copy(
                        type = VariantType.DELETE,
                        canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(affectedExon = 2)
                    )
                )
            ),
            "No gene A activating mutation(s)"
        )
    }

    @Test
    fun `Should pass with activating mutation in ignored exon but different type`() {
        val function = GeneHasActivatingMutation(
            GENE, codonsToIgnore = null, variantTypeToIgnore = VariantTypeInput.DELETE, exonToIgnore = 2
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    ACTIVATING_VARIANT.copy(
                        type = VariantType.INSERT,
                        canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(affectedExon = 2)
                    )
                )
            ),
            "gene A activating mutation(s): event"
        )
    }

    private fun assertResultForVariant(expectedResult: EvaluationResult, variant: Variant, expectedMessage: String) {
        assertResultForVariantWithTML(expectedResult, variant, null, expectedMessage)

        // Repeat with high TML since unknown TML always results in a warning for reportable variants:
        assertResultForVariantWithTML(expectedResult, variant, true, expectedMessage)

        if (expectedResult == EvaluationResult.WARN) {
            assertResultForVariantIgnoringCodons(expectedResult, variant, expectedMessage)
        }
    }

    private fun assertResultForVariantIgnoringCodons(expectedResult: EvaluationResult, variant: Variant, expectedMessage: String) {
        assertResultForVariantWithTMLIgnoringCodons(expectedResult, variant, null, expectedMessage)
        assertResultForVariantWithTMLIgnoringCodons(expectedResult, variant, true, expectedMessage)
    }

    private fun assertResultForVariantWithTML(
        expectedResult: EvaluationResult, variant: Variant, hasHighTML: Boolean?, expectedMessage: String
    ) {
        assertMolecularEvaluation(
            expectedResult,
            functionNotIgnoringCodons.evaluate(MolecularTestFactory.withHasTumorMutationalLoadAndVariants(hasHighTML, variant)),
            expectedMessage
        )
        if (expectedResult == EvaluationResult.WARN) {
            assertResultForVariantWithTMLIgnoringCodons(expectedResult, variant, hasHighTML, expectedMessage)
        }
    }

    private fun assertResultForVariantWithTMLIgnoringCodons(
        expectedResult: EvaluationResult,
        variant: Variant,
        hasHighTML: Boolean?,
        expectedMessage: String
    ) {
        assertMolecularEvaluation(
            expectedResult,
            functionWithCodonsToIgnore.evaluate(MolecularTestFactory.withHasTumorMutationalLoadAndVariants(hasHighTML, variant)),
            expectedMessage
        )
    }

    companion object {
        private const val GENE = "gene A"
        private val CODONS_TO_IGNORE = setOf("A100X", "A200X")
        private val ACTIVATING_VARIANT = TestVariantFactory.createMinimal().copy(
            gene = GENE,
            event = "event",
            isReportable = true,
            driverLikelihood = DriverLikelihood.HIGH,
            geneRole = GeneRole.ONCO,
            proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
            isCancerAssociatedVariant = true,
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
            canonicalImpact = impactWithCodon(100),
            clonalLikelihood = 0.8
        )

        private fun impactWithCodon(affectedCodon: Int) =
            TestTranscriptVariantImpactFactory.createMinimal().copy(affectedCodon = affectedCodon)
    }
}