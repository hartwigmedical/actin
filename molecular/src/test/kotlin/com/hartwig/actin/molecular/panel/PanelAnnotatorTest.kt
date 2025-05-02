package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.PriorSequencingTest
import com.hartwig.actin.datamodel.clinical.SequencedAmplification
import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.clinical.SequencedSkippedExons
import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.PanelGeneSpecification
import com.hartwig.actin.datamodel.molecular.PanelSpecifications
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.TestVariantAlterationFactory
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevelDetails
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestEvidenceDirectionFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestTreatmentEvidenceFactory
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val OTHER_GENE = "other_gene"
private val EMPTY_MATCH = TestClinicalEvidenceFactory.createEmpty()
private val ARCHER_VARIANT = SequencedVariant(gene = GENE, hgvsCodingImpact = HGVS_CODING)
private val ARCHER_FUSION = SequencedFusion(GENE, OTHER_GENE)

private val ON_LABEL_MATCH = TestClinicalEvidenceFactory.withEvidence(
    TestTreatmentEvidenceFactory.create(
        treatment = "treatment",
        evidenceLevel = EvidenceLevel.A,
        evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
        evidenceDirection = TestEvidenceDirectionFactory.certainPositiveResponse(),
        isOnLabel = true
    )
)
private val ARCHER_SKIPPED_EXON = SequencedSkippedExons(GENE, 2, 3)

private const val TEST_NAME = "test"

class PanelAnnotatorTest {

    private val evidenceDatabase = mockk<EvidenceDatabase> {
        every { evidenceForVariant(any()) } returns EMPTY_MATCH
        every { variantAlterationForVariant(any()) } returns TestVariantAlterationFactory.createVariantAlteration(GENE)
    }
    private val panelVariantAnnotator = mockk<PanelVariantAnnotator> {
        every { annotate(any()) } returns emptyList()
    }
    private val panelFusionAnnotator = mockk<PanelFusionAnnotator> {
        every { annotate(any(), any()) } returns emptyList()
    }
    private val panelCopyNumberAnnotator = mockk<PanelCopyNumberAnnotator> {
        every { annotate(any<Set<SequencedAmplification>>()) } returns emptyList()
    }

    private val annotator =
        PanelAnnotator(
            evidenceDatabase,
            panelVariantAnnotator,
            panelFusionAnnotator,
            panelCopyNumberAnnotator,
            PanelSpecifications(mapOf(TEST_NAME to listOf(PanelGeneSpecification(GENE, listOf(MolecularTestTarget.MUTATION)))))
        )

    @Test
    fun `Should annotate test with panel specifications`() {
        val annotatedPanel = annotator.annotate(createTestPriorSequencingTest())
        assertThat(annotatedPanel.testsGene(GENE) { it == listOf(MolecularTestTarget.MUTATION) }).isTrue()
        assertThat(annotatedPanel.testsGene("another gene") { it == listOf(MolecularTestTarget.MUTATION) }).isFalse()
        assertThat(annotatedPanel.testsGene(GENE) { it == listOf(MolecularTestTarget.FUSION) }).isFalse()
    }

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
    fun `Should infer ploidy`() {
        val annotated = annotator.annotate(createTestPriorSequencingTest())
        assertThat(annotated.characteristics.ploidy).isEqualTo(2.0)
    }

    @Test
    fun `Should annotate microsatellite status with evidence`() {
        every { evidenceDatabase.evidenceForMicrosatelliteStatus(true) } returns ON_LABEL_MATCH
        every { evidenceDatabase.evidenceForMicrosatelliteStatus(false) } returns EMPTY_MATCH

        val panelWithMSI = annotator.annotate(createTestPriorSequencingTest().copy(isMicrosatelliteUnstable = true))
        assertThat(panelWithMSI.characteristics.microsatelliteStability!!.evidence).isEqualTo(ON_LABEL_MATCH)

        val panelWithMSS = annotator.annotate(createTestPriorSequencingTest().copy(isMicrosatelliteUnstable = false))
        assertThat(panelWithMSS.characteristics.microsatelliteStability!!.evidence).isEqualTo(EMPTY_MATCH)

        val panelWithoutMicrosatelliteStatus = annotator.annotate(createTestPriorSequencingTest().copy(isMicrosatelliteUnstable = null))
        assertThat(panelWithoutMicrosatelliteStatus.characteristics.microsatelliteStability).isNull()
    }

    @Test
    fun `Should annotate tumor mutational burden with evidence`() {
        every { evidenceDatabase.evidenceForTumorMutationalBurdenStatus(true) } returns ON_LABEL_MATCH
        every { evidenceDatabase.evidenceForTumorMutationalBurdenStatus(false) } returns EMPTY_MATCH

        val panelWithHighTmb = annotator.annotate(createTestPriorSequencingTest().copy(tumorMutationalBurden = 200.0))
        assertThat(panelWithHighTmb.characteristics.tumorMutationalBurden!!.evidence).isEqualTo(ON_LABEL_MATCH)

        val panelWithLowTmb = annotator.annotate(createTestPriorSequencingTest().copy(tumorMutationalBurden = 2.0))
        assertThat(panelWithLowTmb.characteristics.tumorMutationalBurden!!.evidence).isEqualTo(EMPTY_MATCH)

        val panelWithoutTmb = annotator.annotate(createTestPriorSequencingTest().copy(tumorMutationalBurden = null))
        assertThat(panelWithoutTmb.characteristics.tumorMutationalBurden).isNull()
    }

    private fun createTestPriorSequencingTest(): PriorSequencingTest {
        return PriorSequencingTest(test = TEST_NAME)
    }
}