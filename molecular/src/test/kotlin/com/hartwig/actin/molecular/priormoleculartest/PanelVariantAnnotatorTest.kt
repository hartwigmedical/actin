package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.molecular.datamodel.CodingEffect
import com.hartwig.actin.molecular.datamodel.TranscriptImpact
import com.hartwig.actin.molecular.datamodel.VariantType
import com.hartwig.actin.molecular.datamodel.panel.PanelVariantExtraction
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.actin.molecular.paver.PaveCodingEffect
import com.hartwig.actin.molecular.paver.PaveImpact
import com.hartwig.actin.molecular.paver.PaveQuery
import com.hartwig.actin.molecular.paver.PaveResponse
import com.hartwig.actin.molecular.paver.PaveTranscriptImpact
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.tools.pave.ImmutableVariantTranscriptImpact
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.actin.tools.variant.ImmutableVariant
import com.hartwig.actin.tools.variant.VariantAnnotator
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


private const val ALT = "T"
private const val REF = "G"
private const val TRANSCRIPT = "transcript"
private const val GENE_ID = "gene_id"
private const val OTHER_TRANSCRIPT = "other_transcript"
private const val OTHER_GENE = "other_gene"
private const val OTHER_GENE_ID = "other_gene_id"
private const val OTHER_GENE_TRANSCRIPT = "other_gene_transcript"
private const val CHROMOSOME = "1"
private const val POSITION = 1
private val EMPTY_MATCH = ActionabilityMatch(emptyList(), emptyList())
private val ARCHER_VARIANT = PanelVariantExtraction(GENE, HGVS_CODING)
private val ARCHER_PANEL_WITH_VARIANT = ArcherPanelExtraction(variants = listOf(ARCHER_VARIANT))
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

private val TRANSCRIPT_ANNOTATION =
    ImmutableVariant.builder().alt(ALT).ref(REF).chromosome(CHROMOSOME).position(POSITION).build()

private val PAVE_QUERY = PaveQuery(
    id = "0",
    chromosome = CHROMOSOME,
    position = POSITION,
    ref = REF,
    alt = ALT,
)
private val PAVE_LITE_ANNOTATION =
    ImmutableVariantTranscriptImpact.builder().affectedExon(1).affectedCodon(1).build()

private val PAVE_ANNOTATION = PaveResponse(
    id = "0",
    impact = PaveImpact(
        gene = GENE,
        transcript = TRANSCRIPT,
        canonicalEffect = "canonicalEffect",
        canonicalCodingEffect = PaveCodingEffect.MISSENSE,
        spliceRegion = false,
        hgvsCodingImpact = HGVS_CODING,
        hgvsProteinImpact = HGVS_PROTEIN,
        otherReportableEffects = null,
        worstCodingEffect = PaveCodingEffect.MISSENSE,
        genesAffected = 1
    ),
    transcriptImpact = emptyList()
)


class PanelVariantAnnotatorTest {

    private val evidenceDatabase = mockk<EvidenceDatabase> {
        every { evidenceForVariant(any()) } returns EMPTY_MATCH
        every { geneAlterationForVariant(any()) } returns null
    }
    private val geneDriverLikelihoodModel = mockk<GeneDriverLikelihoodModel> {
        every { evaluate(any(), any(), any()) } returns null
    }
    private val transvarAnnotator = mockk<VariantAnnotator> {
        every { resolve(GENE, null, HGVS_CODING) } returns TRANSCRIPT_ANNOTATION
    }
    private val paveLite = mockk<PaveLite> {
        every { run(GENE, TRANSCRIPT, POSITION) } returns PAVE_LITE_ANNOTATION
    }

    private val paver = mockk<Paver> {
        every { run(any<List<PaveQuery>>()) } returns emptyList()
        every { run(listOf(PAVE_QUERY)) } returns listOf(PAVE_ANNOTATION)
    }

    private val annotator = PanelVariantAnnotator(evidenceDatabase, geneDriverLikelihoodModel, transvarAnnotator, paver, paveLite)

    @Test
    fun `Should exclude other transcript impacts from non canonical gene`() {
        val complexPaveAnnotation = PAVE_ANNOTATION.copy(
            transcriptImpact = listOf(
                PaveTranscriptImpact(
                    geneId = OTHER_GENE_ID,
                    gene = OTHER_GENE,
                    transcript = OTHER_GENE_TRANSCRIPT,
                    effects = listOf(),
                    spliceRegion = false,
                    hgvsCodingImpact = HGVS_CODING,
                    hgvsProteinImpact = HGVS_PROTEIN
                )
            )
        )

        val transcriptImpact = annotator.otherImpacts(complexPaveAnnotation, TRANSCRIPT_ANNOTATION)
        assertThat(transcriptImpact).isEqualTo(emptySet<TranscriptImpact>())
    }


    @Test
    fun `Should annotate valid other transcripts with paveLite`() {
        val complexPaveAnnotation = PAVE_ANNOTATION.copy(
            transcriptImpact = listOf(
                PaveTranscriptImpact(
                    geneId = GENE_ID,
                    gene = GENE,
                    transcript = OTHER_TRANSCRIPT,
                    effects = listOf(),
                    spliceRegion = false,
                    hgvsCodingImpact = HGVS_CODING,
                    hgvsProteinImpact = HGVS_PROTEIN
                )
            )
        )

        every { paveLite.run(GENE, OTHER_TRANSCRIPT, POSITION) } returns PAVE_LITE_ANNOTATION

        val transcriptImpact = annotator.otherImpacts(complexPaveAnnotation, TRANSCRIPT_ANNOTATION)
        assertThat(transcriptImpact).isEqualTo(
            setOf(
                TranscriptImpact(
                    transcriptId = OTHER_TRANSCRIPT,
                    hgvsCodingImpact = HGVS_CODING,
                    hgvsProteinImpact = HGVS_PROTEIN,
                    affectedCodon = 1,
                    affectedExon = 1,
                    isSpliceRegion = false,
                    effects = emptySet(),
                    codingEffect = CodingEffect.NONE
                )
            )
        )
    }

    @Test
    fun `Should not run PAVE when no variants`() {
        val variants = emptyList<PanelVariantExtraction>()
        val annotatedVariants = annotator.annotate(variants)
        assertThat(annotatedVariants).isEmpty()
        verify(exactly = 0) { paver.run(any()) }
    }
}