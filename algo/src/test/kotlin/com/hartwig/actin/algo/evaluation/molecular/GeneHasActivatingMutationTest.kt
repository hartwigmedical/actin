package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.CodingEffect
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.TEST_DATE
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.hmf.driver.ExhaustiveVariant
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherVariantExtraction
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelType
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericVariantExtraction
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
    fun `Should pass for gene with mutation in Archer panel and no Orange molecular`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            functionNotIgnoringCodons.evaluate(
                TestPatientFactory.createEmptyMolecularTestPatientRecord().copy(
                    molecularHistory = MolecularHistory(listOf(panelRecord(ARCHER_MOLECULAR_TEST_WITH_ACTIVATING_VARIANT))),
                )
            )
        )
    }

    @Test
    fun `Should pass for gene with mutation in Generic panel and no Orange molecular`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            functionNotIgnoringCodons.evaluate(
                TestPatientFactory.createEmptyMolecularTestPatientRecord().copy(
                    molecularHistory = MolecularHistory(
                        listOf(
                            TestPanelRecordFactory.empty().copy(
                                panelEvents = AVL_PANEL_WITH_ACTIVATING_VARIANT.events(),
                                testedGenes = AVL_PANEL_WITH_ACTIVATING_VARIANT.testedGenes()
                            )
                        )),
                )
            )
        )
    }

    @Test
    fun `Should be undetermined for gene not in Archer panel with no Orange molecular`() {

        val evaluation = functionNotIgnoringCodons.evaluate(
            TestPatientFactory.createEmptyMolecularTestPatientRecord().copy(
                molecularHistory = MolecularHistory(listOf(panelRecord(EMPTY_ARCHER_MOLECULAR_TEST))),
            )
        )

        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedGeneralMessages).containsExactly("Gene $GENE not tested")
    }

    @Test
    fun `Should fail for gene always tested but not returned in Archer panel and no Orange molecular`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            GeneHasActivatingMutation("ALK", null).evaluate(
                TestPatientFactory.createEmptyMolecularTestPatientRecord().copy(
                    molecularHistory = MolecularHistory(listOf(panelRecord(EMPTY_ARCHER_MOLECULAR_TEST))),
                )
            )
        )
    }

    @Test
    fun `Should pass and aggregate findings for gene with mutation in Archer panel and also in Orange molecular`() {
        val base = MolecularTestFactory.withHasTumorMutationalLoadAndVariants(false, ACTIVATING_VARIANT)
        val patient = base.copy(
            molecularHistory = MolecularHistory(
                base.molecularHistory.molecularTests + listOf(panelRecord(ARCHER_MOLECULAR_TEST_WITH_ACTIVATING_VARIANT))
            )
        )

        val evaluation = functionNotIgnoringCodons.evaluate(patient)

        assertMolecularEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passSpecificMessages).size().isEqualTo(2)
        assertThat(evaluation.passGeneralMessages).size().isEqualTo(1)
    }

    @Test
    fun `Should be undetermined for Archer variant on gene but codons to ignore and no Orange molecular`() {
        val patient = TestPatientFactory.createEmptyMolecularTestPatientRecord().copy(
            molecularHistory = MolecularHistory(listOf(panelRecord(ARCHER_MOLECULAR_TEST_WITH_ACTIVATING_VARIANT)))
        )

        val evaluation = functionWithCodonsToIgnore.evaluate(patient)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    private fun assertResultForVariant(expectedResult: EvaluationResult, variant: ExhaustiveVariant) {
        assertResultForVariantWithTML(expectedResult, variant, null)

        // Repeat with high TML since unknown TML always results in a warning for reportable variants:
        assertResultForVariantWithTML(expectedResult, variant, true)

        if (expectedResult == EvaluationResult.WARN) {
            assertResultForVariantIgnoringCodons(expectedResult, variant)
        }
    }

    private fun assertResultForVariantIgnoringCodons(expectedResult: EvaluationResult, variant: ExhaustiveVariant) {
        assertResultForVariantWithTMLIgnoringCodons(expectedResult, variant, null)
        assertResultForVariantWithTMLIgnoringCodons(expectedResult, variant, true)
    }

    private fun assertResultForVariantWithTML(expectedResult: EvaluationResult, variant: ExhaustiveVariant, hasHighTML: Boolean?) {
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
        variant: ExhaustiveVariant,
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
        private val ACTIVATING_VARIANT: ExhaustiveVariant = TestVariantFactory.createMinimal().copy(
            gene = GENE,
            isReportable = true,
            driverLikelihood = DriverLikelihood.HIGH,
            geneRole = GeneRole.ONCO,
            proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
            isHotspot = true,
            isAssociatedWithDrugResistance = false,
            clonalLikelihood = 0.8,
            canonicalImpact = impactWithCodon(300)
        )

        private val ACTIVATING_VARIANT_WITH_CODON_TO_IGNORE: ExhaustiveVariant = TestVariantFactory.createMinimal().copy(
            gene = GENE,
            isReportable = true,
            driverLikelihood = DriverLikelihood.HIGH,
            geneRole = GeneRole.ONCO,
            proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
            isHotspot = true,
            isAssociatedWithDrugResistance = false,
            clonalLikelihood = 0.8,
            canonicalImpact = impactWithCodon(100)
        )

        private fun impactWithCodon(affectedCodon: Int) = TestTranscriptImpactFactory.createMinimal().copy(affectedCodon = affectedCodon)

        private fun panelRecord(extraction: ArcherPanelExtraction) =
            TestPanelRecordFactory.empty().copy(panelEvents = extraction.events(), testedGenes = extraction.testedGenes())

        private val ARCHER_MOLECULAR_TEST_WITH_ACTIVATING_VARIANT = ArcherPanelExtraction(
            variants = listOf(
                ArcherVariantExtraction(
                    gene = GENE,
                    hgvsCodingImpact = "c.1A>T",
                ),
            ),
            fusions = emptyList(),
            skippedExons = emptyList(),
            date = TEST_DATE
        )

        private val EMPTY_ARCHER_MOLECULAR_TEST = ArcherPanelExtraction(
            variants = emptyList(),
            fusions = emptyList(),
            skippedExons = emptyList(),
            date = TEST_DATE
        )

        private val AVL_PANEL_WITH_ACTIVATING_VARIANT = GenericPanelExtraction(
            panelType = GenericPanelType.AVL,
            variants = listOf(
                GenericVariantExtraction(
                    gene = GENE,
                    hgvsCodingImpact = "c.1A>T",
                ),
            ),
            fusions = emptyList(),
            date = TEST_DATE
        )
    }
}