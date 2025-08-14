package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.clinical.SequencedSkippedExons
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.tools.ensemblcache.EnsemblDataCache
import com.hartwig.actin.tools.ensemblcache.TranscriptData
import com.hartwig.hmftools.common.fusion.KnownFusionCache
import com.hartwig.hmftools.common.fusion.KnownFusionType
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val TRANSCRIPT = "transcript"
private const val CANONICAL_TRANSCRIPT = "canonical_transcript"
private const val GENE_START = "gene_start"
private const val GENE_END = "gene_end"
private const val FUSED_EXON_UP = 2
private const val FUSED_EXON_DOWN = 4
private const val EXON_SKIPPED_UP = 6
private const val EXON_SKIPPED_DOWN = 8
private const val TRANSCRIPT_START = "transcript_start"
private const val TRANSCRIPT_END = "transcript_end"
private val SEQUENCED_FUSION = SequencedFusion(GENE_START, GENE_END)
private val FULLY_SPECIFIED_SEQUENCED_FUSION =
    SequencedFusion(GENE_START, GENE_END, TRANSCRIPT_START, TRANSCRIPT_END, FUSED_EXON_UP, FUSED_EXON_DOWN)

class PanelFusionAnnotatorTest {

    private val knownFusionCache = mockk<KnownFusionCache>()
    private val ensembleDataCache = mockk<EnsemblDataCache>()
    private val annotator = PanelFusionAnnotator(knownFusionCache, ensembleDataCache)

    @Test
    fun `Should determine fusion driver likelihood`() {
        assertThat(annotator.fusionDriverLikelihood(FusionDriverType.KNOWN_PAIR, false))
            .isEqualTo(DriverLikelihood.HIGH)

        assertThat(annotator.fusionDriverLikelihood(FusionDriverType.KNOWN_PAIR_IG, false))
            .isEqualTo(DriverLikelihood.HIGH)

        assertThat(annotator.fusionDriverLikelihood(FusionDriverType.KNOWN_PAIR_DEL_DUP, false))
            .isEqualTo(DriverLikelihood.HIGH)

        assertThat(annotator.fusionDriverLikelihood(FusionDriverType.PROMISCUOUS_3, true))
            .isEqualTo(DriverLikelihood.HIGH)

        assertThat(annotator.fusionDriverLikelihood(FusionDriverType.PROMISCUOUS_ENHANCER_TARGET, false))
            .isEqualTo(DriverLikelihood.LOW)

        assertThat(annotator.fusionDriverLikelihood(FusionDriverType.PROMISCUOUS_3, false))
            .isEqualTo(DriverLikelihood.LOW)
    }

    @Test
    fun `Should determine fusion driver type known_pair`() {
        every { knownFusionCache.hasKnownFusion("gene1", "gene2") } returns true

        assertThat(annotator.determineFusionDriverType("gene1", "gene2")).isEqualTo(FusionDriverType.KNOWN_PAIR)
    }

    @Test
    fun `Should determine fusion driver type del_dup`() {
        every { knownFusionCache.hasKnownFusion("gene1", "gene1") } returns false
        every { knownFusionCache.hasExonDelDup("gene1") } returns true

        assertThat(annotator.determineFusionDriverType("gene1", "gene1")).isEqualTo(FusionDriverType.KNOWN_PAIR_DEL_DUP)
    }

    @Test
    fun `Should determine fusion driver type promiscuous_both`() {
        every { knownFusionCache.hasKnownFusion("gene1", "gene2") } returns false
        every { knownFusionCache.hasPromiscuousFiveGene("gene1") } returns true
        every { knownFusionCache.hasPromiscuousThreeGene("gene2") } returns true

        assertThat(annotator.determineFusionDriverType("gene1", "gene2")).isEqualTo(FusionDriverType.PROMISCUOUS_BOTH)
    }

    @Test
    fun `Should determine fusion driver type promiscuous_5`() {
        every { knownFusionCache.hasKnownFusion("gene1", "gene2") } returns false
        every { knownFusionCache.hasPromiscuousFiveGene("gene1") } returns true
        every { knownFusionCache.hasPromiscuousThreeGene("gene2") } returns false

        assertThat(annotator.determineFusionDriverType("gene1", "gene2")).isEqualTo(FusionDriverType.PROMISCUOUS_5)
    }

    @Test
    fun `Should determine fusion driver type promiscuous_3`() {
        every { knownFusionCache.hasKnownFusion("gene1", "gene2") } returns false
        every { knownFusionCache.hasPromiscuousFiveGene("gene1") } returns false
        every { knownFusionCache.hasPromiscuousThreeGene("gene2") } returns true

        assertThat(annotator.determineFusionDriverType("gene1", "gene2")).isEqualTo(FusionDriverType.PROMISCUOUS_3)
    }

    @Test
    fun `Should determine fusion driver type promiscuous_none`() {
        every { knownFusionCache.hasKnownFusion("gene1", "gene2") } returns false
        every { knownFusionCache.hasPromiscuousFiveGene("gene1") } returns false
        every { knownFusionCache.hasPromiscuousThreeGene("gene2") } returns false

        assertThat(annotator.determineFusionDriverType("gene1", "gene2")).isEqualTo(FusionDriverType.NONE)
    }

    @Test
    fun `Should return true for promiscuous 3 fusion within exon range`() {
        val matchingExon = 4
        val fusionMatchingExons = FULLY_SPECIFIED_SEQUENCED_FUSION.copy(exonDown = matchingExon)

        every { ensembleDataCache.findGeneDataByName(GENE_END) } returns mockk {
            every { geneId() } returns "geneId"
        }

        every { ensembleDataCache.findCanonicalTranscript("geneId") } returns mockk<TranscriptData> {
            every { transcriptName() } returns CANONICAL_TRANSCRIPT
            every { exons().size } returns matchingExon
        }

        every {
            knownFusionCache.withinPromiscuousExonRange(
                KnownFusionType.PROMISCUOUS_3,
                CANONICAL_TRANSCRIPT,
                matchingExon,
                matchingExon
            )
        } returns true

        assertThat(annotator.isPromiscuousWithMatchingExons(FusionDriverType.PROMISCUOUS_3, fusionMatchingExons)).isEqualTo(true)
    }

    @Test
    fun `Should return false for promiscuous 3 fusion outside exon range`() {
        val nonMatchingExon = 8
        val fusionNonMatchingExons = FULLY_SPECIFIED_SEQUENCED_FUSION.copy(exonDown = nonMatchingExon)

        every { ensembleDataCache.findGeneDataByName(GENE_END) } returns mockk {
            every { geneId() } returns "geneId"
        }

        every { ensembleDataCache.findCanonicalTranscript("geneId") } returns mockk<TranscriptData> {
            every { transcriptName() } returns CANONICAL_TRANSCRIPT
            every { exons().size } returns nonMatchingExon
        }

        every {
            knownFusionCache.withinPromiscuousExonRange(
                KnownFusionType.PROMISCUOUS_3,
                CANONICAL_TRANSCRIPT,
                nonMatchingExon,
                nonMatchingExon
            )
        } returns false

        assertThat(annotator.isPromiscuousWithMatchingExons(FusionDriverType.PROMISCUOUS_3, fusionNonMatchingExons)).isEqualTo(false)
    }

    @Test
    fun `Should return true for promiscuous 5 fusion within exon range`() {
        val matchingExon = 4
        val fusionMatchingExons = FULLY_SPECIFIED_SEQUENCED_FUSION.copy(exonUp = matchingExon)

        every { ensembleDataCache.findGeneDataByName(GENE_START) } returns mockk {
            every { geneId() } returns "geneId"
        }

        every { ensembleDataCache.findCanonicalTranscript("geneId") } returns mockk<TranscriptData> {
            every { transcriptName() } returns CANONICAL_TRANSCRIPT
            every { exons().size } returns matchingExon
        }

        every {
            knownFusionCache.withinPromiscuousExonRange(
                KnownFusionType.PROMISCUOUS_5,
                CANONICAL_TRANSCRIPT,
                matchingExon,
                matchingExon
            )
        } returns true

        assertThat(annotator.isPromiscuousWithMatchingExons(FusionDriverType.PROMISCUOUS_5, fusionMatchingExons)).isEqualTo(true)
    }

    @Test
    fun `Should return false for promiscuous 5 fusion outside exon range`() {
        val nonMatchingExon = 8
        val fusionNonMatchingExons = FULLY_SPECIFIED_SEQUENCED_FUSION.copy(exonUp = nonMatchingExon)

        every { ensembleDataCache.findGeneDataByName(GENE_START) } returns mockk {
            every { geneId() } returns "geneId"
        }

        every { ensembleDataCache.findCanonicalTranscript("geneId") } returns mockk<TranscriptData> {
            every { transcriptName() } returns CANONICAL_TRANSCRIPT
            every { exons().size } returns nonMatchingExon

        }

        every {
            knownFusionCache.withinPromiscuousExonRange(
                KnownFusionType.PROMISCUOUS_5,
                CANONICAL_TRANSCRIPT,
                nonMatchingExon,
                nonMatchingExon
            )
        } returns false

        assertThat(annotator.isPromiscuousWithMatchingExons(FusionDriverType.PROMISCUOUS_5, fusionNonMatchingExons)).isEqualTo(false)
    }

    @Test
    fun `Should return false if exon is out of canonical transcript range`() {
        every { ensembleDataCache.findGeneDataByName(GENE_START) } returns mockk {
            every { geneId() } returns "geneId"
        }

        every { ensembleDataCache.findCanonicalTranscript("geneId") } returns mockk<TranscriptData> {
            every { transcriptName() } returns CANONICAL_TRANSCRIPT
            every { exons().size } returns 5
        }

        assertThat(
            annotator.isPromiscuousWithMatchingExons(
                FusionDriverType.PROMISCUOUS_5,
                FULLY_SPECIFIED_SEQUENCED_FUSION.copy(exonUp = 8)
            )
        ).isEqualTo(false)
    }

    @Test
    fun `Should return false for known fusion pair`() {
        every { knownFusionCache.withinPromiscuousExonRange(KnownFusionType.PROMISCUOUS_5, TRANSCRIPT_END, 1, 1) } returns false
        assertThat(annotator.isPromiscuousWithMatchingExons(FusionDriverType.KNOWN_PAIR, FULLY_SPECIFIED_SEQUENCED_FUSION)).isEqualTo(false)
    }

    @Test
    fun `Should annotate fusion specified with genes only`() {
        setupKnownFusionCache()
        val annotated = annotator.annotate(setOf(SEQUENCED_FUSION), emptySet())

        assertThat(annotated).containsExactly(
            Fusion(
                geneStart = GENE_START,
                geneEnd = GENE_END,
                driverType = FusionDriverType.KNOWN_PAIR,
                proteinEffect = ProteinEffect.UNKNOWN,
                isAssociatedWithDrugResistance = null,
                geneTranscriptStart = null,
                geneTranscriptEnd = null,
                fusedExonUp = null,
                fusedExonDown = null,
                event = "$GENE_START::$GENE_END fusion",
                isReportable = true,
                driverLikelihood = DriverLikelihood.HIGH,
                evidence = TestClinicalEvidenceFactory.createEmpty()
            )
        )
    }

    @Test
    fun `Should annotate fully specified fusion`() {
        setupKnownFusionCache()
        val annotated = annotator.annotate(setOf(FULLY_SPECIFIED_SEQUENCED_FUSION), emptySet())

        assertThat(annotated).containsExactly(
            Fusion(
                geneStart = GENE_START,
                geneEnd = GENE_END,
                driverType = FusionDriverType.KNOWN_PAIR,
                proteinEffect = ProteinEffect.UNKNOWN,
                isAssociatedWithDrugResistance = null,
                geneTranscriptStart = TRANSCRIPT_START,
                geneTranscriptEnd = TRANSCRIPT_END,
                fusedExonUp = FUSED_EXON_UP,
                fusedExonDown = FUSED_EXON_DOWN,
                event = "$GENE_START(exon$FUSED_EXON_UP)::$GENE_END(exon$FUSED_EXON_DOWN) fusion",
                isReportable = true,
                driverLikelihood = DriverLikelihood.HIGH,
                evidence = TestClinicalEvidenceFactory.createEmpty()
            )
        )
    }

    @Test
    fun `Should annotate to canonical transcript when no transcript provided for exon skip`() {
        setupKnownFusionCacheForExonDeletion()

        every { ensembleDataCache.findCanonicalTranscript("geneId") } returns mockk<TranscriptData> {
            every { transcriptName() } returns CANONICAL_TRANSCRIPT
            every { exons().size } returns EXON_SKIPPED_DOWN
        }

        every { ensembleDataCache.findGeneDataByName(GENE) } returns mockk {
            every { geneId() } returns "geneId"
        }

        val panelSkippedExonsExtraction = setOf(SequencedSkippedExons(GENE, EXON_SKIPPED_UP, EXON_SKIPPED_DOWN, null))
        val fusions = annotator.annotate(emptySet(), panelSkippedExonsExtraction)
        assertThat(fusions).isEqualTo(
            listOf(
                Fusion(
                    geneStart = GENE,
                    geneEnd = GENE,
                    driverType = FusionDriverType.KNOWN_PAIR_DEL_DUP,
                    proteinEffect = ProteinEffect.UNKNOWN,
                    isAssociatedWithDrugResistance = null,
                    geneTranscriptStart = CANONICAL_TRANSCRIPT,
                    geneTranscriptEnd = CANONICAL_TRANSCRIPT,
                    fusedExonUp = EXON_SKIPPED_UP - 1,
                    fusedExonDown = EXON_SKIPPED_DOWN + 1,
                    event = "$GENE exons $EXON_SKIPPED_UP-$EXON_SKIPPED_DOWN skipping",
                    isReportable = true,
                    driverLikelihood = DriverLikelihood.HIGH,
                    evidence = TestClinicalEvidenceFactory.createEmpty()
                )
            )
        )
    }

    @Test
    fun `Should annotate with provided transcript when available`() {
        setupKnownFusionCacheForExonDeletion()

        val panelSkippedExonsExtraction = setOf(SequencedSkippedExons(GENE, EXON_SKIPPED_UP, EXON_SKIPPED_DOWN, TRANSCRIPT))
        val fusions = annotator.annotate(emptySet(), panelSkippedExonsExtraction)
        assertThat(fusions).isEqualTo(
            listOf(
                Fusion(
                    geneStart = GENE,
                    geneEnd = GENE,
                    driverType = FusionDriverType.KNOWN_PAIR_DEL_DUP,
                    proteinEffect = ProteinEffect.UNKNOWN,
                    isAssociatedWithDrugResistance = null,
                    geneTranscriptStart = TRANSCRIPT,
                    geneTranscriptEnd = TRANSCRIPT,
                    fusedExonUp = EXON_SKIPPED_UP - 1,
                    fusedExonDown = EXON_SKIPPED_DOWN + 1,
                    event = "$GENE exons $EXON_SKIPPED_UP-$EXON_SKIPPED_DOWN skipping",
                    isReportable = true,
                    driverLikelihood = DriverLikelihood.HIGH,
                    evidence = TestClinicalEvidenceFactory.createEmpty()
                )
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception if exonUp is curated but geneUp is null`() {
        val fusion = SequencedFusion(geneUp = null, exonUp = 5)
        annotator.annotate(setOf(fusion), emptySet())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception if exonDown is curated but geneDown is null`() {
        val fusion = SequencedFusion(geneDown = null, exonDown = 5)
        annotator.annotate(setOf(fusion), emptySet())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception if both genes are null`() {
        val fusion = SequencedFusion(geneUp = null, geneDown = null)
        annotator.annotate(setOf(fusion), emptySet())
    }

    private fun setupKnownFusionCache() {
        every { knownFusionCache.hasKnownFusion(GENE_START, GENE_END) } returns true
    }

    private fun setupKnownFusionCacheForExonDeletion() {
        every { knownFusionCache.hasKnownFusion(GENE, GENE) } returns false
        every { knownFusionCache.hasExonDelDup(GENE) } returns true
        every { knownFusionCache.hasPromiscuousFiveGene(GENE) } returns false
        every { knownFusionCache.hasPromiscuousThreeGene(GENE) } returns false
    }
}