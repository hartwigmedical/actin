package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedAmplification
import com.hartwig.actin.datamodel.clinical.SequencedDeletion
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestGeneAlterationFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantAlterationFactory
import com.hartwig.actin.datamodel.molecular.driver.TranscriptCopyNumberImpact
import com.hartwig.actin.datamodel.molecular.evidence.CancerTypeMatchApplicability
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevelDetails
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestEvidenceDirectionFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestTreatmentEvidenceFactory
import com.hartwig.actin.molecular.evidence.known.KnownEventResolver
import com.hartwig.actin.tools.ensemblcache.EnsemblDataCache
import com.hartwig.actin.tools.ensemblcache.TranscriptData
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val CANONICAL_TRANSCRIPT = "canonical_transcript"
private const val NON_CANONICAL_TRANSCRIPT = "non_canonical_transcript"

class PanelCopyNumberAnnotatorTest {

    private val ensembleDataCache = mockk<EnsemblDataCache>()

    private val annotator = PanelCopyNumberAnnotator(ensembleDataCache)

    @Test
    fun `Should annotate gene amplification with evidence for canonical transcript`() {
        setupEnsemblDataCacheForCopyNumber()

        val annotatedPanel = annotator.annotate(setOf(SequencedAmplification(GENE, CANONICAL_TRANSCRIPT)))
        val canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN)
            .copy(transcriptId = CANONICAL_TRANSCRIPT, minCopies = 6, maxCopies = 6)
        val otherImpacts = emptySet<TranscriptCopyNumberImpact>()
        check(annotatedPanel, canonicalImpact, otherImpacts, "amp")
    }

    @Test
    fun `Should annotate gene amplification with evidence for non-canonical transcript`() {
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
        setupEnsemblDataCacheForCopyNumber()

        val annotatedPanel = annotator.annotate(setOf(SequencedDeletion(GENE, CANONICAL_TRANSCRIPT)))
        val canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.DEL)
            .copy(transcriptId = CANONICAL_TRANSCRIPT)
        val otherImpacts = emptySet<TranscriptCopyNumberImpact>()
        check(annotatedPanel, canonicalImpact, otherImpacts, "del")
    }

    @Test
    fun `Should annotate gene deletion with evidence for non-canonical transcript`() {
        setupEnsemblDataCacheForCopyNumber()

        val annotatedPanel = annotator.annotate(setOf(SequencedDeletion(GENE, NON_CANONICAL_TRANSCRIPT)))
        val canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.NONE)
            .copy(transcriptId = CANONICAL_TRANSCRIPT, minCopies = 2, maxCopies = 2)
        val otherImpacts = setOf(
            TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.DEL)
                .copy(transcriptId = NON_CANONICAL_TRANSCRIPT)
        )
        check(annotatedPanel, canonicalImpact, otherImpacts, "del")
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
                    evidence = TestClinicalEvidenceFactory.createEmpty(),
                    gene = GENE,
                    geneRole = GeneRole.UNKNOWN,
                    proteinEffect = ProteinEffect.UNKNOWN,
                    isAssociatedWithDrugResistance = null,
                )
            )
        )
    }
}