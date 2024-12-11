package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.clinical.SequencedSkippedExons
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.Fusion
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceDirection
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory.treatment
import com.hartwig.actin.datamodel.molecular.orange.driver.FusionDriverType
import com.hartwig.actin.molecular.GENE
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.actin.molecular.evidence.TestServeFactory
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.actin.molecular.evidence.actionability.ActionableEvents
import com.hartwig.actin.molecular.evidence.matching.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.tools.ensemblcache.EnsemblDataCache
import com.hartwig.actin.tools.ensemblcache.TranscriptData
import com.hartwig.hmftools.common.fusion.KnownFusionCache
import com.hartwig.serve.datamodel.efficacy.EvidenceLevelDetails
import com.hartwig.serve.datamodel.molecular.ImmutableMolecularCriterium
import com.hartwig.serve.datamodel.molecular.gene.ImmutableActionableGene
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.hartwig.serve.datamodel.efficacy.EvidenceDirection as ServeEvidenceDirection
import com.hartwig.serve.datamodel.efficacy.EvidenceLevel as ServeEvidenceLevel

private val EMPTY_MATCH = ActionabilityMatch(ActionableEvents(), ActionableEvents())
private const val TRANSCRIPT = "transcript"
private const val CANONICAL_TRANSCRIPT = "canonical_transcript"
private const val GENE_START = "gene_start"
private const val GENE_END = "gene_end"
private const val FUSED_EXON_UP = 2
private const val FUSED_EXON_DOWN = 4
private const val TRANSCRIPT_START = "transcript_start"
private const val TRANSCRIPT_END = "transcript_end"
private val SEQUENCED_FUSION = SequencedFusion(GENE_START, GENE_END)
private val FULLY_SPECIFIED_SEQUENCED_FUSION =
    SequencedFusion(GENE_START, GENE_END, TRANSCRIPT_START, TRANSCRIPT_END, FUSED_EXON_UP, FUSED_EXON_DOWN)

private val EXON_SKIP_FUSION_MATCHING_CRITERIA = FusionMatchCriteria(
    isReportable = true,
    geneStart = GENE,
    geneEnd = GENE,
    driverType = FusionDriverType.KNOWN_PAIR_DEL_DUP,
    fusedExonUp = FUSED_EXON_UP,
    fusedExonDown = FUSED_EXON_DOWN
)

private val FUSION_MATCH_CRITERIA = FusionMatchCriteria(
    isReportable = true,
    geneStart = GENE_START,
    geneEnd = GENE_END,
    driverType = FusionDriverType.KNOWN_PAIR
)

private val FULLY_SPECIFIED_FUSION_MATCH_CRITERIA = FusionMatchCriteria(
    isReportable = true,
    geneStart = GENE_START,
    geneEnd = GENE_END,
    driverType = FusionDriverType.KNOWN_PAIR,
    fusedExonUp = FUSED_EXON_UP,
    fusedExonDown = FUSED_EXON_DOWN
)

private val MOLECULAR_CRITERIUM = ImmutableMolecularCriterium.builder().addGenes(
    ImmutableActionableGene.builder().from(TestServeActionabilityFactory.createActionableEvent())
        .from(TestServeFactory.createEmptyGeneAnnotation()).build()
).build()

private val ACTIONABILITY_MATCH = ActionabilityMatch(
    onLabelEvidence = ActionableEvents(
        listOf(
            TestServeActionabilityFactory.createEfficacyEvidence(
                MOLECULAR_CRITERIUM,
                level = ServeEvidenceLevel.A,
                direction = ServeEvidenceDirection.RESPONSIVE
            )
        ), emptyList()
    ),
    offLabelEvidence = ActionableEvents()
)

class PanelFusionAnnotatorTest {

    private val evidenceDatabase = mockk<EvidenceDatabase> {
        every { evidenceForVariant(any()) } returns EMPTY_MATCH
        every { geneAlterationForVariant(any()) } returns null
    }

    private val knownFusionCache = mockk<KnownFusionCache>()

    private val ensembleDataCache = mockk<EnsemblDataCache>()

    private val annotator = PanelFusionAnnotator(evidenceDatabase, knownFusionCache, ensembleDataCache)

    @Test
    fun `Should determine fusion driver likelihood`() {
        assertThat(annotator.fusionDriverLikelihood(FusionDriverType.KNOWN_PAIR))
            .isEqualTo(DriverLikelihood.HIGH)

        assertThat(annotator.fusionDriverLikelihood(FusionDriverType.KNOWN_PAIR_IG))
            .isEqualTo(DriverLikelihood.HIGH)

        assertThat(annotator.fusionDriverLikelihood(FusionDriverType.KNOWN_PAIR_DEL_DUP))
            .isEqualTo(DriverLikelihood.HIGH)

        assertThat(annotator.fusionDriverLikelihood(FusionDriverType.PROMISCUOUS_ENHANCER_TARGET))
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
    fun `Should annotate fusion specified with genes only`() {
        setupKnownFusionCache()
        setupEvidenceForFusion(FUSION_MATCH_CRITERIA)
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
                evidence = ClinicalEvidence(
                    treatmentEvidence = setOf(
                        treatment(
                            treatment = "treatment",
                            evidenceLevel = EvidenceLevel.A,
                            evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
                            direction = EvidenceDirection(hasPositiveResponse = true, isCertain = true, hasBenefit = true),
                            onLabel = true,
                            isCategoryEvent = true
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should annotate fully specified fusion`() {
        setupKnownFusionCache()
        setupEvidenceForFusion(FULLY_SPECIFIED_FUSION_MATCH_CRITERIA)
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
                event = "$GENE_START::$GENE_END fusion",
                isReportable = true,
                driverLikelihood = DriverLikelihood.HIGH,
                evidence = ClinicalEvidence(
                    treatmentEvidence = setOf(
                        treatment(
                            treatment = "treatment",
                            evidenceLevel = EvidenceLevel.A,
                            evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
                            direction = EvidenceDirection(hasPositiveResponse = true, isCertain = true, hasBenefit = true),
                            onLabel = true,
                            isCategoryEvent = true
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should annotate to canonical transcript when no transcript provided for exon skip`() {
        setupKnownFusionCacheForExonDeletion()
        setupEvidenceDatabaseWithNoEvidence()

        every { ensembleDataCache.findCanonicalTranscript("geneId") } returns mockk<TranscriptData> {
            every { transcriptName() } returns CANONICAL_TRANSCRIPT
        }

        every { ensembleDataCache.findGeneDataByName(GENE) } returns mockk {
            every { geneId() } returns "geneId"
        }

        val panelSkippedExonsExtraction = setOf(SequencedSkippedExons(GENE, 2, 4, null))
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
                    fusedExonUp = FUSED_EXON_UP,
                    fusedExonDown = FUSED_EXON_DOWN,
                    event = "$GENE skipped exons $FUSED_EXON_UP-$FUSED_EXON_DOWN",
                    isReportable = true,
                    driverLikelihood = DriverLikelihood.HIGH,
                    evidence = ClinicalEvidence()
                )
            )
        )
    }

    @Test
    fun `Should annotate with provided transcript when available`() {
        setupKnownFusionCacheForExonDeletion()
        setupEvidenceDatabaseWithNoEvidence()

        val panelSkippedExonsExtraction = setOf(SequencedSkippedExons(GENE, FUSED_EXON_UP, FUSED_EXON_DOWN, TRANSCRIPT))
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
                    fusedExonUp = FUSED_EXON_UP,
                    fusedExonDown = FUSED_EXON_DOWN,
                    event = "$GENE skipped exons $FUSED_EXON_UP-$FUSED_EXON_DOWN",
                    isReportable = true,
                    driverLikelihood = DriverLikelihood.HIGH,
                    evidence = ClinicalEvidence()
                )
            )
        )
    }

    private fun setupKnownFusionCache() {
        every { knownFusionCache.hasKnownFusion(GENE_START, GENE_END) } returns true
    }

    private fun setupEvidenceForFusion(fusionMatchCriteria: FusionMatchCriteria) {
        every { evidenceDatabase.lookupKnownFusion(fusionMatchCriteria) } returns null
        every { evidenceDatabase.evidenceForFusion(fusionMatchCriteria) } returns ACTIONABILITY_MATCH
    }

    private fun setupKnownFusionCacheForExonDeletion() {
        every { knownFusionCache.hasKnownFusion(GENE, GENE) } returns false
        every { knownFusionCache.hasExonDelDup(GENE) } returns true
        every { knownFusionCache.hasPromiscuousFiveGene(GENE) } returns false
        every { knownFusionCache.hasPromiscuousThreeGene(GENE) } returns false
    }

    private fun setupEvidenceDatabaseWithNoEvidence() {
        every { evidenceDatabase.lookupKnownFusion(EXON_SKIP_FUSION_MATCHING_CRITERIA) } returns null
        every { evidenceDatabase.evidenceForFusion(EXON_SKIP_FUSION_MATCHING_CRITERIA) } returns ActionabilityMatch(
            onLabelEvidence = ActionableEvents(),
            offLabelEvidence = ActionableEvents()
        )
    }
}