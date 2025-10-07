package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedAmplification
import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.clinical.SequencedSkippedExons
import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.clinical.SequencedVirus
import com.hartwig.actin.datamodel.clinical.SequencingTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.panel.PanelTestSpecification
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.datamodel.molecular.driver.VirusType
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val OTHER_GENE = "other_gene"
private val ARCHER_VARIANT = SequencedVariant(gene = GENE, hgvsCodingImpact = HGVS_CODING)
private val ARCHER_FUSION = SequencedFusion(GENE, OTHER_GENE)
private val ARCHER_SKIPPED_EXON = SequencedSkippedExons(GENE, 2, 3)
private val PANEL_VIRUS = SequencedVirus(type = VirusType.HPV, isLowRisk = false)

private const val TEST_NAME = "test"

class PanelAnnotatorTest {

    private val panelVariantAnnotator = mockk<PanelVariantAnnotator> {
        every { annotate(any()) } returns emptyList()
    }
    private val panelFusionAnnotator = mockk<PanelFusionAnnotator> {
        every { annotate(any(), any()) } returns emptyList()
    }
    private val panelCopyNumberAnnotator = mockk<PanelCopyNumberAnnotator> {
        every { annotate(any<Set<SequencedAmplification>>()) } returns emptyList()
    }
    private val panelDriverAttributeAnnotator = mockk<PanelDriverAttributeAnnotator>() {
        every { annotate(any()) } answers { firstArg() }
    }
    private val panelVirusAnnotator = mockk<PanelVirusAnnotator> {
        every { annotate(any<Set<SequencedVirus>>()) } returns emptyList()
    }

    private val annotator =
        PanelAnnotator(
            LocalDate.of(2025, 7, 1),
            panelVariantAnnotator,
            panelFusionAnnotator,
            panelCopyNumberAnnotator,
            panelVirusAnnotator,
            panelDriverAttributeAnnotator,
            PanelSpecifications(
                setOf(GENE, OTHER_GENE),
                mapOf(
                    PanelTestSpecification(TEST_NAME) to listOf(
                        PanelGeneSpecification(
                            GENE,
                            listOf(MolecularTestTarget.MUTATION)
                        )
                    )
                )
            )
        )

    @Test
    fun `Should annotate test with panel specifications`() {
        val annotatedPanel = annotator.annotate(createTestSequencingTest())
        assertThat(annotatedPanel.testsGene(GENE) { it == listOf(MolecularTestTarget.MUTATION) }).isTrue()
        assertThat(annotatedPanel.testsGene("another gene") { it == listOf(MolecularTestTarget.MUTATION) }).isFalse()
        assertThat(annotatedPanel.testsGene(GENE) { it == listOf(MolecularTestTarget.FUSION) }).isFalse()
    }

    @Test
    fun `Should annotate variant`() {
        val expected = mockk<Variant>()
        every { panelVariantAnnotator.annotate(setOf(ARCHER_VARIANT)) } returns listOf(expected)

        val annotatedPanel = annotator.annotate(createTestSequencingTest().copy(variants = setOf(ARCHER_VARIANT)))
        assertThat(annotatedPanel.drivers.variants).isEqualTo(listOf(expected))
    }

    @Test
    fun `Should annotate fusion`() {
        val annotatedFusion = TestMolecularFactory.createMinimalFusion().copy(
            geneStart = GENE,
            geneEnd = OTHER_GENE,
        )

        every { panelFusionAnnotator.annotate(setOf(ARCHER_FUSION), emptySet()) } returns listOf(annotatedFusion)

        val annotatedPanel = annotator.annotate(createTestSequencingTest().copy(fusions = setOf(ARCHER_FUSION)))
        assertThat(annotatedPanel.drivers.fusions).isEqualTo(listOf(annotatedFusion))
    }

    @Test
    fun `Should annotate exon skip`() {
        val expected = mockk<Fusion>()
        every { panelFusionAnnotator.annotate(emptySet(), setOf(ARCHER_SKIPPED_EXON)) } returns listOf(expected)

        val annotatedPanel = annotator.annotate(createTestSequencingTest().copy(skippedExons = setOf(ARCHER_SKIPPED_EXON)))
        assertThat(annotatedPanel.drivers.fusions).isEqualTo(listOf(expected))
    }

    @Test
    fun `Should annotate virus`() {
        val expected = mockk<Virus>()
        every { panelVirusAnnotator.annotate(setOf(PANEL_VIRUS)) } returns listOf(expected)

        val annotatedPanel = annotator.annotate(createTestSequencingTest().copy(viruses = setOf(PANEL_VIRUS)))
        assertThat(annotatedPanel.drivers.viruses).isEqualTo(listOf(expected))
    }

    private fun createTestSequencingTest(): SequencingTest {
        return SequencingTest(test = TEST_NAME, knownSpecifications = true)
    }
}
