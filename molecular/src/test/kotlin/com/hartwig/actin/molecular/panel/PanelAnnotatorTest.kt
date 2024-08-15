package com.hartwig.actin.molecular.panel

import com.hartwig.actin.clinical.datamodel.PriorSequencingTest
import com.hartwig.actin.clinical.datamodel.SequencedAmplification
import com.hartwig.actin.clinical.datamodel.SequencedFusion
import com.hartwig.actin.clinical.datamodel.SequencedSkippedExons
import com.hartwig.actin.clinical.datamodel.SequencedVariant
import com.hartwig.actin.molecular.datamodel.CodingEffect
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.Fusion
import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.Variant
import com.hartwig.actin.molecular.datamodel.VariantType
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumberType
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.actin.molecular.evidence.actionability.TestServeActionabilityFactory
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.serve.datamodel.EvidenceDirection
import com.hartwig.serve.datamodel.EvidenceLevel
import com.hartwig.serve.datamodel.Knowledgebase
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.hartwig.serve.datamodel.common.GeneRole as ServeGeneRole
import com.hartwig.serve.datamodel.common.ProteinEffect as ServeProteinEffect

private const val ALT = "T"
private const val REF = "G"
private const val OTHER_GENE = "other_gene"
private const val CHROMOSOME = "1"
private const val POSITION = 1
private val EMPTY_MATCH = ActionabilityMatch(emptyList(), emptyList())
private val ARCHER_VARIANT = SequencedVariant(GENE, HGVS_CODING)
private val VARIANT_MATCH_CRITERIA =
    VariantMatchCriteria(
        isReportable = true,
        gene = GENE,
        chromosome = CHROMOSOME,
        ref = REF,
        alt = ALT,
        position = POSITION,
        codingEffect = CodingEffect.MISSENSE,
        type = VariantType.SNV
    )
private val ARCHER_FUSION = SequencedFusion(GENE, OTHER_GENE)

private val HOTSPOT = TestServeKnownFactory.hotspotBuilder().build()
    .withGeneRole(ServeGeneRole.ONCO)
    .withProteinEffect(ServeProteinEffect.GAIN_OF_FUNCTION)

private val ACTIONABILITY_MATCH = ActionabilityMatch(
    onLabelEvents = listOf(
        TestServeActionabilityFactory.geneBuilder().build().withSource(Knowledgebase.CKB_EVIDENCE).withLevel(EvidenceLevel.A)
            .withDirection(EvidenceDirection.RESPONSIVE)
    ), offLabelEvents = emptyList()
)

private val ARCHER_SKIPPED_EXON = SequencedSkippedExons(GENE, 2, 3)

class PanelAnnotatorTest {

    private val evidenceDatabase = mockk<EvidenceDatabase> {
        every { evidenceForVariant(any()) } returns EMPTY_MATCH
        every { geneAlterationForVariant(any()) } returns null
    }
    private val panelVariantAnnotator = mockk<PanelVariantAnnotator> {
        every { annotate(any()) } returns emptySet()
    }
    private val panelFusionAnnotator = mockk<PanelFusionAnnotator> {
        every { annotate(any(), any()) } returns emptySet()
    }

    private val annotator =
        PanelAnnotator(
            evidenceDatabase,
            panelVariantAnnotator,
            panelFusionAnnotator
        )

    @Test
    fun `Should annotate variant`() {
        val expected = mockk<Variant>()
        every { panelVariantAnnotator.annotate(setOf(ARCHER_VARIANT)) } returns setOf(expected)

        val annotatedPanel = annotator.annotate(PriorSequencingTest(test = "test", variants = setOf(ARCHER_VARIANT)))
        assertThat(annotatedPanel.drivers.variants).isEqualTo(setOf(expected))
    }

    @Test
    fun `Should annotate fusion`() {
        val expected = mockk<Fusion>()
        every { panelFusionAnnotator.annotate(setOf(ARCHER_FUSION), emptySet()) } returns setOf(expected)

        val annotatedPanel = annotator.annotate(PriorSequencingTest(test = "test", fusions = setOf(ARCHER_FUSION)))
        assertThat(annotatedPanel.drivers.fusions).isEqualTo(setOf(expected))
    }

    @Test
    fun `Should annotate exon skip`() {
        val expected = mockk<Fusion>()
        every { panelFusionAnnotator.annotate(emptySet(), setOf(ARCHER_SKIPPED_EXON)) } returns setOf(expected)

        val annotatedPanel = annotator.annotate(PriorSequencingTest(test = "test", skippedExons = setOf(ARCHER_SKIPPED_EXON)))
        assertThat(annotatedPanel.drivers.fusions).isEqualTo(setOf(expected))
    }

    @Test
    fun `Should infer copy numbers and annotate with evidence from serve`() {
        setupGeneAlteration()
        val unannotatedCopyNumberSlot = mutableListOf<CopyNumber>()
        every { evidenceDatabase.geneAlterationForCopyNumber(capture(unannotatedCopyNumberSlot)) } returns HOTSPOT
        every { evidenceDatabase.evidenceForCopyNumber(capture(unannotatedCopyNumberSlot)) } returns ACTIONABILITY_MATCH

        val annotated = annotator.annotate(PriorSequencingTest(test = "test", amplifications = setOf(SequencedAmplification(GENE))))
        val annotatedVariant = annotated.drivers.copyNumbers.first()
        assertCopyNumber(unannotatedCopyNumberSlot[0])
        assertCopyNumber(unannotatedCopyNumberSlot[1])
        assertCopyNumber(annotatedVariant)
        assertThat(annotatedVariant.geneRole).isEqualTo(GeneRole.ONCO)
        assertThat(annotatedVariant.proteinEffect).isEqualTo(ProteinEffect.GAIN_OF_FUNCTION)
    }

    private fun assertCopyNumber(annotatedVariant: CopyNumber) {
        assertThat(annotatedVariant.minCopies).isEqualTo(6)
        assertThat(annotatedVariant.maxCopies).isEqualTo(6)
        assertThat(annotatedVariant.type).isEqualTo(CopyNumberType.FULL_GAIN)
        assertThat(annotatedVariant.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
        assertThat(annotatedVariant.isReportable).isTrue()
    }

    private fun setupGeneAlteration() {
        every { evidenceDatabase.geneAlterationForVariant(VARIANT_MATCH_CRITERIA) } returns HOTSPOT
    }
}