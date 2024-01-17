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
    fun `Should fail for minimal patient`() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }

    @Test
    fun `Should pass with activating mutation for gene`() {
        assertResultForVariant(EvaluationResult.PASS, ACTIVATING_VARIANT)
    }

    @Test
    fun `Should fail with activating mutation for other gene`() {
        assertResultForVariant(EvaluationResult.FAIL, ACTIVATING_VARIANT.copy(gene = "gene B"))
    }

    @Test
    fun `Should warn with activating mutation for TSG`() {
        assertResultForVariant(EvaluationResult.WARN, ACTIVATING_VARIANT.copy(geneRole = GeneRole.TSG))
    }

    @Test
    fun `Should warn with activating mutation with drug resistance for gene`() {
        assertResultForVariant(EvaluationResult.WARN, ACTIVATING_VARIANT.copy(isAssociatedWithDrugResistance = true))
    }

    @Test
    fun `Should warn with activating mutation for gene with no protein effect or hotspot`() {
        assertResultForVariant(EvaluationResult.WARN, ACTIVATING_VARIANT.copy(proteinEffect = ProteinEffect.UNKNOWN, isHotspot = false))
    }

    @Test
    fun `Should warn with activating mutation for gene with low driver likelihood`() {
        assertResultForVariant(EvaluationResult.WARN, ACTIVATING_VARIANT.copy(driverLikelihood = DriverLikelihood.LOW))
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
                isHotspot = false,
                canonicalImpact = TestTranscriptImpactFactory.createMinimal().copy(codingEffect = CodingEffect.MISSENSE)
            )
        )
    }

    @Test
    fun `Should warn with non reportable hotspot mutation for gene`() {
        assertResultForVariant(
            EvaluationResult.WARN, TestVariantFactory.createMinimal().copy(gene = GENE, isReportable = false, isHotspot = true)
        )
    }

    @Test
    fun `Should warn with high driver subclonal activating mutation for gene`() {
        assertResultForVariant(
            EvaluationResult.WARN,
            TestVariantFactory.createMinimal().copy(
                gene = GENE, isReportable = true, driverLikelihood = DriverLikelihood.HIGH, clonalLikelihood = 0.2
            ),
        )
    }

    @Test
    fun `Should warn with low driver subclonal activating mutation for gene and unknown TML`() {
        assertResultForVariantWithTML(
            EvaluationResult.WARN,
            TestVariantFactory.createMinimal().copy(
                gene = GENE, isReportable = true, driverLikelihood = DriverLikelihood.LOW, clonalLikelihood = 0.2
            ),
            null
        )
    }

    @Test
    fun `Should fail with low driver subclonal activating mutation for gene and high TML`() {
        assertResultForVariantWithTML(
            EvaluationResult.FAIL,
            TestVariantFactory.createMinimal().copy(
                gene = GENE, isReportable = true, driverLikelihood = DriverLikelihood.LOW, clonalLikelihood = 0.2
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
        private val ACTIVATING_VARIANT: Variant = TestVariantFactory.createMinimal().copy(
            gene = GENE,
            isReportable = true,
            driverLikelihood = DriverLikelihood.HIGH,
            geneRole = GeneRole.ONCO,
            proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
            isHotspot = true,
            isAssociatedWithDrugResistance = false,
            clonalLikelihood = 0.8
        )
    }
}