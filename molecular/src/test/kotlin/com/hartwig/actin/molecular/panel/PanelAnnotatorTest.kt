package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.PriorSequencingTest
import com.hartwig.actin.datamodel.clinical.SequencedAmplification
import com.hartwig.actin.datamodel.clinical.SequencedDeletedGene
import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.clinical.SequencedSkippedExons
import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.molecular.CodingEffect
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.Fusion
import com.hartwig.actin.datamodel.molecular.GeneRole
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.datamodel.molecular.VariantType
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.actin.molecular.GENE
import com.hartwig.actin.molecular.HGVS_CODING
import com.hartwig.actin.molecular.evidence.ClinicalEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.actin.molecular.evidence.actionability.TestActionabilityMatchFactory
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import com.hartwig.actin.molecular.evidence.matching.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.hartwig.serve.datamodel.molecular.common.GeneRole as ServeGeneRole
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect as ServeProteinEffect

private const val CHROMOSOME = "1"
private const val POSITION = 1
private const val REF = "G"
private const val ALT = "T"

private val ARCHER_VARIANT = SequencedVariant(gene = GENE, hgvsCodingImpact = HGVS_CODING)
private val VARIANT_MATCH_CRITERIA =
    VariantMatchCriteria(
        isReportable = true,
        gene = GENE,
        codingEffect = CodingEffect.MISSENSE,
        type = VariantType.SNV,
        chromosome = CHROMOSOME,
        position = POSITION,
        ref = REF,
        alt = ALT
    )

private const val OTHER_GENE = "other_gene"
private val ARCHER_FUSION = SequencedFusion(GENE, OTHER_GENE)

private val HOTSPOT = TestServeKnownFactory.hotspotBuilder().build()
    .withGeneRole(ServeGeneRole.ONCO)
    .withProteinEffect(ServeProteinEffect.GAIN_OF_FUNCTION)

private val EMPTY_MATCH = TestActionabilityMatchFactory.createEmpty()
private val ON_LABEL_MATCH = TestActionabilityMatchFactory.withOnLabelEvidence(
    TestServeActionabilityFactory.createEvidenceForGene()
)

private val ARCHER_SKIPPED_EXON = SequencedSkippedExons(GENE, 2, 3)

class PanelAnnotatorTest {

    private val evidenceDatabase = mockk<EvidenceDatabase> {
        every { evidenceForVariant(any()) } returns EMPTY_MATCH
        every { geneAlterationForVariant(any()) } returns null
    }
    private val panelVariantAnnotator = mockk<PanelVariantAnnotator> {
        every { annotate(any()) } returns emptyList()
    }
    private val panelFusionAnnotator = mockk<PanelFusionAnnotator> {
        every { annotate(any(), any()) } returns emptyList()
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
        every { panelVariantAnnotator.annotate(setOf(ARCHER_VARIANT)) } returns listOf(expected)

        val annotatedPanel = annotator.annotate(createTestPriorSequencingTest().copy(variants = setOf(ARCHER_VARIANT)))
        assertThat(annotatedPanel.drivers.variants).isEqualTo(listOf(expected))
    }

    @Test
    fun `Should annotate fusion`() {
        val expected = mockk<Fusion>()
        every { panelFusionAnnotator.annotate(setOf(ARCHER_FUSION), emptySet()) } returns listOf(expected)

        val annotatedPanel = annotator.annotate(createTestPriorSequencingTest().copy(fusions = setOf(ARCHER_FUSION)))
        assertThat(annotatedPanel.drivers.fusions).isEqualTo(listOf(expected))
    }

    @Test
    fun `Should annotate exon skip`() {
        val expected = mockk<Fusion>()
        every { panelFusionAnnotator.annotate(emptySet(), setOf(ARCHER_SKIPPED_EXON)) } returns listOf(expected)

        val annotatedPanel = annotator.annotate(createTestPriorSequencingTest().copy(skippedExons = setOf(ARCHER_SKIPPED_EXON)))
        assertThat(annotatedPanel.drivers.fusions).isEqualTo(listOf(expected))
    }

    @Test
    fun `Should infer copy numbers and ploidy and annotate with evidence`() {
        every { evidenceDatabase.geneAlterationForVariant(VARIANT_MATCH_CRITERIA) } returns HOTSPOT

        val unannotatedCopyNumberSlot = mutableListOf<CopyNumber>()
        every { evidenceDatabase.geneAlterationForCopyNumber(capture(unannotatedCopyNumberSlot)) } returns HOTSPOT
        every { evidenceDatabase.evidenceForCopyNumber(capture(unannotatedCopyNumberSlot)) } returns ON_LABEL_MATCH

        val annotated = annotator.annotate(createTestPriorSequencingTest().copy(amplifications = setOf(SequencedAmplification(GENE))))
        val annotatedVariant = annotated.drivers.copyNumbers.first()
        assertCopyNumber(unannotatedCopyNumberSlot[0])
        assertCopyNumber(unannotatedCopyNumberSlot[1])
        assertCopyNumber(annotatedVariant)
        assertThat(annotated.characteristics.ploidy).isEqualTo(2.0)
        assertThat(annotatedVariant.geneRole).isEqualTo(GeneRole.ONCO)
        assertThat(annotatedVariant.proteinEffect).isEqualTo(ProteinEffect.GAIN_OF_FUNCTION)
    }

    @Test
    fun `Should annotate gene deletion with evidence`() {
        every { evidenceDatabase.geneAlterationForCopyNumber(any()) } returns HOTSPOT
        every { evidenceDatabase.evidenceForCopyNumber(any()) } returns ON_LABEL_MATCH

        val annotatedPanel = annotator.annotate(createTestPriorSequencingTest().copy(deletedGenes = setOf(SequencedDeletedGene(GENE))))
        assertThat(annotatedPanel.drivers.copyNumbers).isEqualTo(
            listOf(
                CopyNumber(
                    type = CopyNumberType.LOSS,
                    minCopies = 0,
                    maxCopies = 0,
                    isReportable = true,
                    event = GENE,
                    driverLikelihood = DriverLikelihood.HIGH,
                    evidence = ClinicalEvidenceFactory.create(ON_LABEL_MATCH),
                    gene = GENE,
                    geneRole = GeneRole.ONCO,
                    proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
                    isAssociatedWithDrugResistance = null,
                )
            )
        )
    }

    @Test
    fun `Should annotate tumor mutational burden with evidence`() {
        every { evidenceDatabase.evidenceForTumorMutationalBurdenStatus(true) } returns ON_LABEL_MATCH
        every { evidenceDatabase.evidenceForTumorMutationalBurdenStatus(false) } returns EMPTY_MATCH

        val panelWithHighTmb = annotator.annotate(createTestPriorSequencingTest().copy(tumorMutationalBurden = 200.0))
        assertThat(panelWithHighTmb.characteristics.tumorMutationalBurdenEvidence)
            .isEqualTo(ClinicalEvidenceFactory.create(ON_LABEL_MATCH))

        val panelWithLowTmb = annotator.annotate(createTestPriorSequencingTest().copy(tumorMutationalBurden = 2.0))
        assertThat(panelWithLowTmb.characteristics.tumorMutationalBurdenEvidence).isEqualTo(ClinicalEvidenceFactory.create(EMPTY_MATCH))

        val panelWithoutTmb = annotator.annotate(createTestPriorSequencingTest().copy(tumorMutationalBurden = null))
        assertThat(panelWithoutTmb.characteristics.tumorMutationalBurdenEvidence).isNull()
    }

    @Test
    fun `Should annotate microsatellite status with evidence`() {
        every { evidenceDatabase.evidenceForMicrosatelliteStatus(true) } returns ON_LABEL_MATCH
        every { evidenceDatabase.evidenceForMicrosatelliteStatus(false) } returns EMPTY_MATCH

        val panelWithMSI = annotator.annotate(createTestPriorSequencingTest().copy(isMicrosatelliteUnstable = true))
        assertThat(panelWithMSI.characteristics.microsatelliteEvidence).isEqualTo(ClinicalEvidenceFactory.create(ON_LABEL_MATCH))

        val panelWithMSS = annotator.annotate(createTestPriorSequencingTest().copy(isMicrosatelliteUnstable = false))
        assertThat(panelWithMSS.characteristics.microsatelliteEvidence).isEqualTo(ClinicalEvidenceFactory.create(EMPTY_MATCH))

        val panelWithoutMicrosatelliteStatus = annotator.annotate(createTestPriorSequencingTest().copy(isMicrosatelliteUnstable = null))
        assertThat(panelWithoutMicrosatelliteStatus.characteristics.microsatelliteEvidence).isNull()
    }

    private fun assertCopyNumber(annotatedVariant: CopyNumber) {
        assertThat(annotatedVariant.minCopies).isEqualTo(6)
        assertThat(annotatedVariant.maxCopies).isEqualTo(6)
        assertThat(annotatedVariant.type).isEqualTo(CopyNumberType.FULL_GAIN)
        assertThat(annotatedVariant.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
        assertThat(annotatedVariant.isReportable).isTrue()
    }

    private fun createTestPriorSequencingTest(): PriorSequencingTest {
        return PriorSequencingTest(test = "test")
    }
}