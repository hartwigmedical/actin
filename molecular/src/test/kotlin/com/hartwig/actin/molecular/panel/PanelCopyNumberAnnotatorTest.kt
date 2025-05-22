package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedAmplification
import com.hartwig.actin.datamodel.clinical.SequencedDeletion
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevelDetails
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestEvidenceDirectionFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestTreatmentEvidenceFactory
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.TestGeneAlterationFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantAlterationFactory
import com.hartwig.actin.datamodel.molecular.driver.TranscriptCopyNumberImpact
import com.hartwig.actin.datamodel.molecular.evidence.CancerTypeMatchApplicability
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.tools.ensemblcache.EnsemblDataCache
import com.hartwig.actin.tools.ensemblcache.TranscriptData
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val CANONICAL_TRANSCRIPT = "canonical_transcript"
private const val NON_CANONICAL_TRANSCRIPT = "non_canonical_transcript"
private const val AMP_COPY_NR = 10

private val EMPTY_MATCH = TestClinicalEvidenceFactory.createEmpty()
private val AMPLIFICATION = TestGeneAlterationFactory.createGeneAlteration("gene 1", GeneRole.ONCO, ProteinEffect.GAIN_OF_FUNCTION, null)
private val ACTIONABILITY_MATCH = TestClinicalEvidenceFactory.withEvidence(
    TestTreatmentEvidenceFactory.create(
        treatment = "treatment",
        evidenceLevel = EvidenceLevel.A,
        evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
        evidenceDirection = TestEvidenceDirectionFactory.certainPositiveResponse(),
        cancerTypeMatchApplicability = CancerTypeMatchApplicability.SPECIFIC_TYPE
    )
)

class PanelCopyNumberAnnotatorTest {

    private val evidenceDatabase = mockk<EvidenceDatabase> {
        every { evidenceForVariant(any()) } returns EMPTY_MATCH
        every { alterationForVariant(any()) } returns TestVariantAlterationFactory.createVariantAlteration(GENE)
    }
    private val ensembleDataCache = mockk<EnsemblDataCache>()
    private val annotator = PanelCopyNumberAnnotator(evidenceDatabase, ensembleDataCache)

    @Test
    fun `Should annotate gene amplification with copy nr and evidence for canonical transcript`() {
        setupEvidenceForCopyNumber()
        setupEnsemblDataCacheForCopyNumber()

        val annotatedPanel = annotator.annotate(setOf(SequencedAmplification(GENE, CANONICAL_TRANSCRIPT, AMP_COPY_NR)))
        val canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN)
            .copy(transcriptId = CANONICAL_TRANSCRIPT, minCopies = AMP_COPY_NR, maxCopies = AMP_COPY_NR)
        val otherImpacts = emptySet<TranscriptCopyNumberImpact>()
        check(annotatedPanel, canonicalImpact, otherImpacts, "amp")
    }

    @Test
    fun `Should annotate gene amplification with copy nr and evidence for non-canonical transcript`() {
        setupEvidenceForCopyNumber()
        setupEnsemblDataCacheForCopyNumber()

        val annotatedPanel = annotator.annotate(setOf(SequencedAmplification(GENE, NON_CANONICAL_TRANSCRIPT, AMP_COPY_NR)))
        val canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.NONE)
            .copy(transcriptId = CANONICAL_TRANSCRIPT, minCopies = null, maxCopies = null)
        val otherImpacts = setOf(
            TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN)
                .copy(transcriptId = NON_CANONICAL_TRANSCRIPT, minCopies = AMP_COPY_NR, maxCopies = AMP_COPY_NR)
        )
        check(annotatedPanel, canonicalImpact, otherImpacts, "amp")
    }

    @Test
    fun `Should annotate gene amplification with evidence for canonical transcript and if copies is null`() {
        setupEvidenceForCopyNumber()
        setupEnsemblDataCacheForCopyNumber()

        val annotatedPanel = annotator.annotate(setOf(SequencedAmplification(GENE, CANONICAL_TRANSCRIPT)))
        val canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN)
            .copy(transcriptId = CANONICAL_TRANSCRIPT, minCopies = null, maxCopies = null)
        val otherImpacts = emptySet<TranscriptCopyNumberImpact>()
        check(annotatedPanel, canonicalImpact, otherImpacts, "amp")
    }

    @Test
    fun `Should annotate gene amplification with evidence for non-canonical transcript and if copies is null`() {
        setupEvidenceForCopyNumber()
        setupEnsemblDataCacheForCopyNumber()

        val annotatedPanel = annotator.annotate(setOf(SequencedAmplification(GENE, NON_CANONICAL_TRANSCRIPT)))
        val canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.NONE)
            .copy(transcriptId = CANONICAL_TRANSCRIPT, minCopies = null, maxCopies = null)
        val otherImpacts = setOf(
            TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN)
                .copy(transcriptId = NON_CANONICAL_TRANSCRIPT, minCopies = null, maxCopies = null)
        )
        check(annotatedPanel, canonicalImpact, otherImpacts, "amp")
    }

    @Test
    fun `Should annotate gene deletion with evidence and min and max copy nr 0 for canonical transcript`() {
        setupEvidenceForCopyNumber()
        setupEnsemblDataCacheForCopyNumber()

        val annotatedPanel = annotator.annotate(setOf(SequencedDeletion(GENE, CANONICAL_TRANSCRIPT)))
        val canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.DEL)
            .copy(transcriptId = CANONICAL_TRANSCRIPT, minCopies = 0, maxCopies = 0)
        val otherImpacts = emptySet<TranscriptCopyNumberImpact>()
        check(annotatedPanel, canonicalImpact, otherImpacts, "del")
    }

    @Test
    fun `Should annotate gene deletion with evidence and min and max copy nr 0 for non-canonical transcript`() {
        setupEvidenceForCopyNumber()
        setupEnsemblDataCacheForCopyNumber()

        val annotatedPanel = annotator.annotate(setOf(SequencedDeletion(GENE, NON_CANONICAL_TRANSCRIPT)))
        val canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.NONE)
            .copy(transcriptId = CANONICAL_TRANSCRIPT, minCopies = null, maxCopies = null)
        val otherImpacts = setOf(
            TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.DEL)
                .copy(transcriptId = NON_CANONICAL_TRANSCRIPT, minCopies = 0, maxCopies = 0)
        )
        check(annotatedPanel, canonicalImpact, otherImpacts, "del")
    }

    private fun setupEvidenceForCopyNumber() {
        every { evidenceDatabase.alterationForCopyNumber(any()) } returns AMPLIFICATION
        every { evidenceDatabase.evidenceForCopyNumber(any()) } returns ACTIONABILITY_MATCH
    }

    private fun setupEnsemblDataCacheForCopyNumber() {
        every { ensembleDataCache.findGeneDataByName(GENE) } returns mockk {
            every { geneId() } returns "geneId"
        }
        every { ensembleDataCache.findCanonicalTranscript("geneId") } returns mockk<TranscriptData> {
            every { transcriptName() } returns CANONICAL_TRANSCRIPT
        }
    }

    private fun check(
        panel: List<CopyNumber>,
        canonicalImpact: TranscriptCopyNumberImpact,
        otherImpacts: Set<TranscriptCopyNumberImpact>,
        type: String
    ) {
        assertThat(panel).isEqualTo(
            listOf(
                CopyNumber(
                    canonicalImpact = canonicalImpact,
                    otherImpacts = otherImpacts,
                    isReportable = true,
                    event = "$GENE $type",
                    driverLikelihood = DriverLikelihood.HIGH,
                    evidence = ACTIONABILITY_MATCH,
                    gene = GENE,
                    geneRole = GeneRole.ONCO,
                    proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
                    isAssociatedWithDrugResistance = null,
                )
            )
        )
    }
}