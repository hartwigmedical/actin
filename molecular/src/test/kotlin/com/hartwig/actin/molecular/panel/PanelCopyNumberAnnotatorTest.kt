package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedAmplification
import com.hartwig.actin.datamodel.clinical.SequencedDeletedGene
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.GeneRole
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevelDetails
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestEvidenceDirectionFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestTreatmentEvidenceFactory
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.orange.driver.TranscriptCopyNumberImpact
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import com.hartwig.actin.tools.ensemblcache.EnsemblDataCache
import com.hartwig.actin.tools.ensemblcache.TranscriptData
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.hartwig.serve.datamodel.molecular.common.GeneRole as ServeGeneRole
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect as ServeProteinEffect

private const val CANONICAL_TRANSCRIPT = "canonical_transcript"
private const val NON_CANONICAL_TRANSCRIPT = "non_canonical_transcript"
private val EMPTY_MATCH = TestClinicalEvidenceFactory.createEmpty()
private val AMPLIFICATION = TestServeKnownFactory.copyNumberBuilder().build().withGeneRole(ServeGeneRole.ONCO)
    .withProteinEffect(ServeProteinEffect.GAIN_OF_FUNCTION)

private val ACTIONABILITY_MATCH = TestClinicalEvidenceFactory.withEvidence(
    TestTreatmentEvidenceFactory.create(
        treatment = "treatment",
        evidenceLevel = EvidenceLevel.A,
        evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
        evidenceDirection = TestEvidenceDirectionFactory.certainPositiveResponse(),
        isOnLabel = true,
        isCategoryEvent = true
    )
)

class PanelCopyNumberAnnotatorTest {

    private val evidenceDatabase = mockk<EvidenceDatabase> {
        every { evidenceForVariant(any()) } returns EMPTY_MATCH
        every { geneAlterationForVariant(any()) } returns null
    }

    private val ensembleDataCache = mockk<EnsemblDataCache>()

    private val annotator = PanelCopyNumberAnnotator(evidenceDatabase, ensembleDataCache)

    @Test
    fun `Should annotate gene amplification with evidence for canonical transcript`() {
        setupEvidenceForCopyNumber()
        setupEnsemblDataCacheForCopyNumber()

        val annotatedPanel = annotator.annotate(setOf(SequencedAmplification(GENE, CANONICAL_TRANSCRIPT)))
        val canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN)
            .copy(transcriptId = CANONICAL_TRANSCRIPT, minCopies = 6, maxCopies = 6)
        val otherImpacts = emptySet<TranscriptCopyNumberImpact>()
        check(annotatedPanel, canonicalImpact, otherImpacts, "amp")
    }

    @Test
    fun `Should annotate gene amplification with evidence for non-canonical transcript`() {
        setupEvidenceForCopyNumber()
        setupEnsemblDataCacheForCopyNumber()

        val annotatedPanel = annotator.annotate(setOf(SequencedAmplification(GENE, NON_CANONICAL_TRANSCRIPT)))
        val canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.NONE)
            .copy(transcriptId = CANONICAL_TRANSCRIPT, minCopies = 2, maxCopies = 2)
        val otherImpacts = setOf(
            TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN)
                .copy(transcriptId = NON_CANONICAL_TRANSCRIPT, minCopies = 6, maxCopies = 6)
        )
        check(annotatedPanel, canonicalImpact, otherImpacts, "amp")
    }

    @Test
    fun `Should annotate gene deletion with evidence for canonical transcript`() {
        setupEvidenceForCopyNumber()
        setupEnsemblDataCacheForCopyNumber()

        val annotatedPanel = annotator.annotate(setOf(SequencedDeletedGene(GENE, CANONICAL_TRANSCRIPT)))
        val canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.LOSS)
            .copy(transcriptId = CANONICAL_TRANSCRIPT)
        val otherImpacts = emptySet<TranscriptCopyNumberImpact>()
        check(annotatedPanel, canonicalImpact, otherImpacts, "del")
    }

    @Test
    fun `Should annotate gene deletion with evidence for non-canonical transcript`() {
        setupEvidenceForCopyNumber()
        setupEnsemblDataCacheForCopyNumber()

        val annotatedPanel = annotator.annotate(setOf(SequencedDeletedGene(GENE, NON_CANONICAL_TRANSCRIPT)))
        val canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.NONE)
            .copy(transcriptId = CANONICAL_TRANSCRIPT, minCopies = 2, maxCopies = 2)
        val otherImpacts = setOf(
            TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.LOSS)
                .copy(transcriptId = NON_CANONICAL_TRANSCRIPT)
        )
        check(annotatedPanel, canonicalImpact, otherImpacts, "del")
    }

    private fun setupEvidenceForCopyNumber() {
        every { evidenceDatabase.geneAlterationForCopyNumber(any()) } returns AMPLIFICATION
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