package com.hartwig.actin.molecular.panel

import com.hartwig.actin.clinical.datamodel.SequencedFusion
import com.hartwig.actin.clinical.datamodel.SequencedSkippedExons
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.Fusion
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceDirection
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceLevel
import com.hartwig.actin.molecular.datamodel.evidence.TestClinicalEvidenceFactory.treatment
import com.hartwig.actin.molecular.datamodel.orange.driver.ExtendedFusionDetails
import com.hartwig.actin.molecular.datamodel.orange.driver.FusionDriverType
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.actin.molecular.evidence.matching.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.tools.ensemblcache.EnsemblDataCache
import com.hartwig.actin.tools.ensemblcache.TranscriptData
import com.hartwig.hmftools.common.fusion.KnownFusionCache
import com.hartwig.serve.datamodel.Knowledgebase
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.hartwig.serve.datamodel.EvidenceDirection as ServeEvidenceDirection
import com.hartwig.serve.datamodel.EvidenceLevel as ServeEvidenceLevel

private val EMPTY_MATCH = ActionabilityMatch(emptyList(), emptyList())
private const val TRANSCRIPT = "transcript"
private const val CANONICAL_TRANSCRIPT = "canonical_transcript"
private const val OTHER_GENE = "other_gene"
private val ARCHER_FUSION = SequencedFusion(GENE, OTHER_GENE)

private val FUSION_MATCHING_CRITERIA = FusionMatchCriteria(
    isReportable = true,
    geneStart = GENE,
    geneEnd = GENE,
    driverType = FusionDriverType.KNOWN_PAIR_DEL_DUP
)

private val FUSION_MATCH_CRITERIA = FusionMatchCriteria(
    isReportable = true,
    geneStart = GENE,
    geneEnd = OTHER_GENE,
    driverType = FusionDriverType.KNOWN_PAIR
)

private val ACTIONABILITY_MATCH = ActionabilityMatch(
    onLabelEvents = listOf(
        TestServeActionabilityFactory.geneBuilder().build().withSource(Knowledgebase.CKB_EVIDENCE).withLevel(ServeEvidenceLevel.A)
            .withDirection(ServeEvidenceDirection.RESPONSIVE)
    ), offLabelEvents = emptyList()
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
        assertThat(annotator.determineFusionDriverType("gene1", "gene2"))
            .isEqualTo(FusionDriverType.KNOWN_PAIR)
    }


    @Test
    fun `Should determine fusion driver type del_dup`() {

        every { knownFusionCache.hasKnownFusion("gene1", "gene1") } returns false
        every { knownFusionCache.hasExonDelDup("gene1") } returns true
        assertThat(annotator.determineFusionDriverType("gene1", "gene1"))
            .isEqualTo(FusionDriverType.KNOWN_PAIR_DEL_DUP)
    }

    @Test
    fun `Should determine fusion driver type promiscuous_both`() {

        every { knownFusionCache.hasKnownFusion("gene1", "gene2") } returns false
        every { knownFusionCache.hasPromiscuousFiveGene("gene1") } returns true
        every { knownFusionCache.hasPromiscuousThreeGene("gene2") } returns true
        assertThat(annotator.determineFusionDriverType("gene1", "gene2"))
            .isEqualTo(FusionDriverType.PROMISCUOUS_BOTH)
    }

    @Test
    fun `Should determine fusion driver type promiscuous_5`() {

        every { knownFusionCache.hasKnownFusion("gene1", "gene2") } returns false
        every { knownFusionCache.hasPromiscuousFiveGene("gene1") } returns true
        every { knownFusionCache.hasPromiscuousThreeGene("gene2") } returns false
        assertThat(annotator.determineFusionDriverType("gene1", "gene2"))
            .isEqualTo(FusionDriverType.PROMISCUOUS_5)
    }

    @Test
    fun `Should determine fusion driver type promiscuous_3`() {

        every { knownFusionCache.hasKnownFusion("gene1", "gene2") } returns false
        every { knownFusionCache.hasPromiscuousFiveGene("gene1") } returns false
        every { knownFusionCache.hasPromiscuousThreeGene("gene2") } returns true
        assertThat(annotator.determineFusionDriverType("gene1", "gene2"))
            .isEqualTo(FusionDriverType.PROMISCUOUS_3)
    }

    @Test
    fun `Should determine fusion driver type promiscuous_none`() {
        every { knownFusionCache.hasKnownFusion("gene1", "gene2") } returns false
        every { knownFusionCache.hasPromiscuousFiveGene("gene1") } returns false
        every { knownFusionCache.hasPromiscuousThreeGene("gene2") } returns false
        assertThat(annotator.determineFusionDriverType("gene1", "gene2"))
            .isEqualTo(FusionDriverType.NONE)
    }

    @Test
    fun `Should annotate fusion`() {
        setupKnownFusionCache()
        setupEvidenceForFusion()
        val annotated = annotator.annotate(setOf(ARCHER_FUSION), emptySet())
        assertThat(annotated).containsExactly(
            Fusion(
                geneStart = GENE,
                geneEnd = OTHER_GENE,
                driverType = FusionDriverType.KNOWN_PAIR,
                proteinEffect = ProteinEffect.UNKNOWN,
                isAssociatedWithDrugResistance = null,
                extendedFusionDetails = null,
                event = "$GENE-$OTHER_GENE fusion",
                isReportable = true,
                driverLikelihood = DriverLikelihood.HIGH,
                evidence = ClinicalEvidence(
                    treatmentEvidence = setOf(
                        treatment(
                            treatment = "intervention",
                            evidenceLevel = EvidenceLevel.A,
                            direction = EvidenceDirection(hasPositiveResponse = true, isCertain = true, hasBenefit = true),
                            onLabel = true,
                            isCategoryVariant = true
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
            setOf(
                Fusion(
                    geneStart = GENE,
                    geneEnd = GENE,
                    driverType = FusionDriverType.KNOWN_PAIR_DEL_DUP,
                    proteinEffect = ProteinEffect.UNKNOWN,
                    isAssociatedWithDrugResistance = null,
                    extendedFusionDetails = ExtendedFusionDetails(CANONICAL_TRANSCRIPT, CANONICAL_TRANSCRIPT, 2, 4),
                    event = "$GENE skipped exons 2-4",
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

        val panelSkippedExonsExtraction = setOf(SequencedSkippedExons(GENE, 2, 4, TRANSCRIPT))
        val fusions = annotator.annotate(emptySet(), panelSkippedExonsExtraction)
        assertThat(fusions).isEqualTo(
            setOf(
                Fusion(
                    geneStart = GENE,
                    geneEnd = GENE,
                    driverType = FusionDriverType.KNOWN_PAIR_DEL_DUP,
                    proteinEffect = ProteinEffect.UNKNOWN,
                    isAssociatedWithDrugResistance = null,
                    extendedFusionDetails = ExtendedFusionDetails(TRANSCRIPT, TRANSCRIPT, 2, 4),
                    event = "$GENE skipped exons 2-4",
                    isReportable = true,
                    driverLikelihood = DriverLikelihood.HIGH,
                    evidence = ClinicalEvidence()
                )
            )
        )
    }

    private fun setupKnownFusionCache() {
        every { knownFusionCache.hasKnownFusion(GENE, OTHER_GENE) } returns true
    }

    private fun setupEvidenceForFusion() {
        every { evidenceDatabase.lookupKnownFusion(FUSION_MATCH_CRITERIA) } returns null
        every { evidenceDatabase.evidenceForFusion(FUSION_MATCH_CRITERIA) } returns ACTIONABILITY_MATCH
    }

    private fun setupKnownFusionCacheForExonDeletion() {
        every { knownFusionCache.hasKnownFusion(GENE, GENE) } returns false
        every { knownFusionCache.hasExonDelDup(GENE) } returns true
        every { knownFusionCache.hasPromiscuousFiveGene(GENE) } returns false
        every { knownFusionCache.hasPromiscuousThreeGene(GENE) } returns false
    }

    private fun setupEvidenceDatabaseWithNoEvidence() {
        every { evidenceDatabase.lookupKnownFusion(FUSION_MATCHING_CRITERIA) } returns null
        every { evidenceDatabase.evidenceForFusion(FUSION_MATCHING_CRITERIA) } returns ActionabilityMatch(
            onLabelEvents = emptyList(),
            offLabelEvents = emptyList()
        )
    }
}