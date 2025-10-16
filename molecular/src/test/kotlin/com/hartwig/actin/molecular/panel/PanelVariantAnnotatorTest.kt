package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.TranscriptVariantImpact
import com.hartwig.actin.datamodel.molecular.driver.VariantEffect
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.molecular.paver.PaveCodingEffect
import com.hartwig.actin.molecular.paver.PaveImpact
import com.hartwig.actin.molecular.paver.PaveQuery
import com.hartwig.actin.molecular.paver.PaveResponse
import com.hartwig.actin.molecular.paver.PaveTranscriptImpact
import com.hartwig.actin.molecular.paver.PaveVariantEffect
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.tools.pave.ImmutableVariantTranscriptImpact
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.actin.tools.variant.ImmutableVariant
import com.hartwig.actin.tools.variant.VariantAnnotator
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

private const val TRANSCRIPT = "transcript"
private const val GENE_ID = "gene_id"
private const val OTHER_TRANSCRIPT = "other_transcript"
private const val OTHER_GENE = "other_gene"
private const val OTHER_GENE_ID = "other_gene_id"
private const val OTHER_GENE_TRANSCRIPT = "other_gene_transcript"
private const val HGVS_PROTEIN_3LETTER = "p.Met1Leu"
private const val HGVS_PROTEIN_1LETTER = "p.M1L"
private val ARCHER_VARIANT = SequencedVariant(gene = GENE, hgvsCodingImpact = HGVS_CODING)

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
        canonicalTranscript = TRANSCRIPT,
        canonicalEffects = listOf(PaveVariantEffect.MISSENSE),
        canonicalCodingEffect = PaveCodingEffect.MISSENSE,
        spliceRegion = false,
        hgvsCodingImpact = HGVS_CODING,
        hgvsProteinImpact = HGVS_PROTEIN_3LETTER,
        otherReportableEffects = null,
        worstCodingEffect = PaveCodingEffect.MISSENSE,
        genesAffected = 1
    ),
    transcriptImpacts = listOf(
        PaveTranscriptImpact(
            geneId = "1",
            gene = GENE,
            transcript = TRANSCRIPT,
            effects = listOf(PaveVariantEffect.MISSENSE),
            spliceRegion = false,
            hgvsCodingImpact = HGVS_CODING,
            hgvsProteinImpact = HGVS_PROTEIN_3LETTER
        )
    )
)

class PanelVariantAnnotatorTest {

    private val transvarAnnotator = mockk<VariantAnnotator> {
        every { resolve(any(), null, HGVS_CODING) } returns TRANSCRIPT_ANNOTATION
    }
    private val paveLite = mockk<PaveLite> {
        every { run(any(), TRANSCRIPT, POSITION) } returns PAVE_LITE_ANNOTATION
    }
    private val paver = mockk<Paver> {
        every { run(any<List<PaveQuery>>()) } returns emptyList()
        every { run(listOf(PAVE_QUERY)) } returns listOf(PAVE_ANNOTATION)
    }
    private val annotator = PanelVariantAnnotator(transvarAnnotator, paver, paveLite)

    @Test
    fun `Should annotate variants with transcript, genetic variation and genomic position`() {
        val annotated = annotator.annotate(setOf(ARCHER_VARIANT)).first()
        assertThat(annotated.chromosome).isEqualTo(CHROMOSOME)
        assertThat(annotated.position).isEqualTo(POSITION)
        assertThat(annotated.ref).isEqualTo(REF)
        assertThat(annotated.alt).isEqualTo(ALT)
        assertThat(annotated.type).isEqualTo(VariantType.SNV)
        assertThat(annotated.variantAlleleFrequency).isNull()
        assertThat(annotated.canonicalImpact.transcriptId).isEqualTo(TRANSCRIPT)
        assertThat(annotated.canonicalImpact.hgvsCodingImpact).isEqualTo(HGVS_CODING)
        assertThat(annotated.canonicalImpact.codingEffect).isEqualTo(CodingEffect.MISSENSE)
        assertThat(annotated.canonicalImpact.hgvsProteinImpact).isEqualTo(HGVS_PROTEIN_1LETTER)
        assertThat(annotated.canonicalImpact.inSpliceRegion).isFalse()
        assertThat(annotated.otherImpacts).isEmpty()
        assertThat(annotated.isBiallelic).isNull()
        assertThat(annotated.event).isEqualTo("$GENE ${HGVS_PROTEIN_1LETTER.removePrefix("p.")}")
        assertThat(annotated.sourceEvent).isEqualTo("$GENE $HGVS_CODING")
    }

    @Test
    fun `Should annotate with vaf and isBiallelic if known`() {
        val annotated = annotator.annotate(setOf(ARCHER_VARIANT.copy(variantAlleleFrequency = 0.5, isBiallelic = true))).first()
        assertThat(annotated.variantAlleleFrequency).isEqualTo(0.5)
        assertThat(annotated.isBiallelic).isTrue()
    }

    @Test
    fun `Should throw exception on null output from transcript annotator`() {
        every { transvarAnnotator.resolve(GENE, null, HGVS_CODING) } returns null
        assertThatThrownBy { annotator.annotate(setOf(ARCHER_VARIANT)) }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `Should annotate variants with affected exon and codon`() {
        every { paveLite.run(GENE, TRANSCRIPT, POSITION) } returns
                ImmutableVariantTranscriptImpact.builder().affectedExon(1).affectedCodon(2).build()
        val annotated = annotator.annotate(setOf(ARCHER_VARIANT))
        val annotatedVariant = annotated.first()
        assertThat(annotatedVariant.canonicalImpact.affectedExon).isEqualTo(1)
        assertThat(annotatedVariant.canonicalImpact.affectedCodon).isEqualTo(2)
    }

    @Test
    fun `Should exclude other transcript impacts from non canonical gene`() {
        val complexPaveAnnotation = PAVE_ANNOTATION.copy(
            transcriptImpacts = listOf(
                paveTranscriptImpact(OTHER_GENE_ID, OTHER_GENE, OTHER_GENE_TRANSCRIPT)
            )
        )

        val transcriptImpact = annotator.otherImpacts(complexPaveAnnotation, TRANSCRIPT_ANNOTATION)
        assertThat(transcriptImpact).isEqualTo(emptySet<TranscriptVariantImpact>())
    }

    @Test
    fun `Should annotate valid other transcripts with paveLite`() {
        val complexPaveAnnotation = PAVE_ANNOTATION.copy(transcriptImpacts = listOf(paveTranscriptImpact()))

        every { paveLite.run(GENE, OTHER_TRANSCRIPT, POSITION) } returns PAVE_LITE_ANNOTATION

        val transcriptImpact = annotator.otherImpacts(complexPaveAnnotation, TRANSCRIPT_ANNOTATION)
        assertThat(transcriptImpact).containsExactly(transcriptVariantImpact(emptySet(), CodingEffect.NONE))
    }

    @Test
    fun `Should retain all effects data and have complete annotation of variants`() {
        val complexPaveAnnotation = PAVE_ANNOTATION.copy(
            transcriptImpacts = listOf(
                paveTranscriptImpact(
                    effects = listOf(PaveVariantEffect.OTHER, PaveVariantEffect.MISSENSE, PaveVariantEffect.INTRONIC)
                )
            )
        )

        every { paveLite.run(GENE, OTHER_TRANSCRIPT, POSITION) } returns PAVE_LITE_ANNOTATION

        val transcriptImpact = annotator.otherImpacts(complexPaveAnnotation, TRANSCRIPT_ANNOTATION)
        assertThat(transcriptImpact).containsExactly(
            transcriptVariantImpact(setOf(VariantEffect.OTHER, VariantEffect.MISSENSE, VariantEffect.INTRONIC), CodingEffect.MISSENSE)
        )
    }

    @Test
    fun `Should not run PAVE when no variants`() {
        val variants = emptySet<SequencedVariant>()
        val annotatedVariants = annotator.annotate(variants)
        assertThat(annotatedVariants).isEmpty()
        verify(exactly = 0) { paver.run(any()) }
    }

    @Test
    fun `Should describe variant event using protein HGVS`() {
        val variants = setOf(SequencedVariant(gene = GENE, hgvsCodingImpact = HGVS_CODING))
        val annotated = annotator.annotate(variants).first()
        assertThat(annotated.event).isEqualTo("$GENE ${HGVS_PROTEIN_1LETTER.removePrefix("p.")}")
    }

    @Test
    fun `Should describe variant using coding HGVS for event when no protein impact`() {
        every { paver.run(listOf(PAVE_QUERY)) } returns listOf(
            PAVE_ANNOTATION.copy(
                impact = PAVE_ANNOTATION.impact.copy(hgvsProteinImpact = "p.?")
            )
        )

        val variants = setOf(SequencedVariant(gene = GENE, hgvsCodingImpact = HGVS_CODING))
        val annotated = annotator.annotate(variants).first()
        assertThat(annotated.event).isEqualTo("$GENE $HGVS_CODING")
    }

    @Test
    fun `Should determine impact from PaveResponse`() {
        assertThat(
            eventString(
                PAVE_ANNOTATION.copy(
                    impact = minimalPaveImpact().copy(
                        canonicalCodingEffect = PaveCodingEffect.SPLICE,
                        hgvsCodingImpact = "c.MUTATION",
                        hgvsProteinImpact = "",
                    )
                )
            )
        ).isEqualTo("c.MUTATION splice")

        assertThat(
            eventString(
                PAVE_ANNOTATION.copy(
                    impact = minimalPaveImpact().copy(
                        canonicalCodingEffect = PaveCodingEffect.NONE,
                        hgvsCodingImpact = "c.C_MUTATION",
                        hgvsProteinImpact = "p.P_MUTATION",
                    )
                )
            )
        ).isEqualTo("P_MUTATION")

        assertThat(
            eventString(
                PAVE_ANNOTATION.copy(
                    impact = minimalPaveImpact().copy(
                        canonicalEffects = listOf(PaveVariantEffect.UPSTREAM_GENE),
                        canonicalCodingEffect = PaveCodingEffect.NONE,
                        hgvsCodingImpact = "",
                        hgvsProteinImpact = "",
                    )
                )
            )
        ).isEqualTo("upstream")

        assertThat(
            eventString(
                PAVE_ANNOTATION.copy(
                    impact = minimalPaveImpact().copy(
                        canonicalEffects = listOf(PaveVariantEffect.INTRONIC, PaveVariantEffect.OTHER),
                        canonicalCodingEffect = PaveCodingEffect.NONE,
                        hgvsCodingImpact = "",
                        hgvsProteinImpact = "",
                    )
                )
            )
        ).isEqualTo("INTRONIC&OTHER")
    }

    @Test
    fun `Should set source event using transcript with matching input transcript`() {
        val sequencedVariant =
            SequencedVariant(gene = GENE, transcript = TRANSCRIPT, hgvsCodingImpact = "c.10T>A", hgvsProteinImpact = null)
        val transcriptImpacts = listOf(
            paveTranscriptImpact(
                transcript = OTHER_TRANSCRIPT,
                hgvsCodingImpact = "wrong",
                hgvsProteinImpact = "wrong",
                effects = listOf(PaveVariantEffect.OTHER)
            ),
            paveTranscriptImpact(
                transcript = TRANSCRIPT,
                hgvsCodingImpact = "c.100T>A",
                hgvsProteinImpact = "p.V44E",
                effects = listOf(PaveVariantEffect.SPLICE_DONOR)
            ),
        )

        setupSourceEventFixture(sequencedVariant, paveCanonicalTranscript = OTHER_TRANSCRIPT, paveTranscriptImpacts = transcriptImpacts)
        val annotated = annotator.annotate(setOf(sequencedVariant)).first()

        assertThat(annotated.sourceEvent).isEqualTo("$GENE c.10T>A splice")
    }

    @Test
    fun `Should set source event using transcript with matching coding hgvs`() {
        val sequencedVariant = SequencedVariant(gene = GENE, transcript = null, hgvsCodingImpact = "c.10T>A", hgvsProteinImpact = null)
        val transcriptImpacts = listOf(
            paveTranscriptImpact(
                transcript = OTHER_TRANSCRIPT,
                hgvsCodingImpact = "mismatch",
                hgvsProteinImpact = "mismatch",
                effects = listOf(PaveVariantEffect.OTHER)
            ),
            paveTranscriptImpact(
                transcript = TRANSCRIPT,
                hgvsCodingImpact = "c.10T>A",
                hgvsProteinImpact = "p.V4E",
                effects = listOf(PaveVariantEffect.SPLICE_DONOR)
            )
        )

        setupSourceEventFixture(sequencedVariant, paveCanonicalTranscript = OTHER_TRANSCRIPT, paveTranscriptImpacts = transcriptImpacts)
        val annotated = annotator.annotate(setOf(sequencedVariant)).first()

        assertThat(annotated.sourceEvent).isEqualTo("$GENE c.10T>A splice")
    }

    @Test
    fun `Should set source event using transcript with matching protein hgvs`() {
        val sequencedVariant = SequencedVariant(gene = GENE, transcript = null, hgvsCodingImpact = "c.9T>A", hgvsProteinImpact = "p.?")
        val transcriptImpacts = listOf(
            paveTranscriptImpact(
                transcript = OTHER_TRANSCRIPT,
                hgvsCodingImpact = "mismatch",
                hgvsProteinImpact = "mismatch",
                effects = listOf(PaveVariantEffect.OTHER)
            ),
            paveTranscriptImpact(
                transcript = TRANSCRIPT,
                hgvsCodingImpact = "c.10T>A",
                hgvsProteinImpact = "p.?",
                effects = listOf(PaveVariantEffect.SPLICE_DONOR)
            )
        )

        setupSourceEventFixture(sequencedVariant, paveCanonicalTranscript = OTHER_TRANSCRIPT, paveTranscriptImpacts = transcriptImpacts)
        val annotated = annotator.annotate(setOf(sequencedVariant)).first()

        assertThat(annotated.sourceEvent).isEqualTo("$GENE c.9T>A splice")
    }

    @Test
    fun `Should set source event using canonical transcript impact when no transcript or protein match`() {
        val sequencedVariant =
            SequencedVariant(gene = GENE, transcript = TRANSCRIPT, hgvsCodingImpact = "c.10T>A", hgvsProteinImpact = null)
        val transcriptImpacts = listOf(
            paveTranscriptImpact(
                transcript = OTHER_TRANSCRIPT,
                hgvsCodingImpact = "ignore",
                hgvsProteinImpact = "ignore",
                effects = listOf(PaveVariantEffect.SPLICE_DONOR)
            ),
        )

        setupSourceEventFixture(sequencedVariant, paveCanonicalTranscript = OTHER_TRANSCRIPT, paveTranscriptImpacts = transcriptImpacts)
        val annotated = annotator.annotate(setOf(sequencedVariant)).first()

        assertThat(annotated.sourceEvent).isEqualTo("$GENE c.10T>A splice")
    }

    private fun setupSourceEventFixture(
        sequencedVariant: SequencedVariant,
        paveCanonicalTranscript: String,
        paveTranscriptImpacts: List<PaveTranscriptImpact>,
    ) {
        every {
            transvarAnnotator.resolve(GENE, sequencedVariant.transcript, sequencedVariant.hgvsCodingOrProteinImpact())
        } returns TRANSCRIPT_ANNOTATION

        every { paver.run(listOf(PAVE_QUERY)) } returns listOf(
            PAVE_ANNOTATION.copy(
                impact = PAVE_ANNOTATION.impact.copy(
                    canonicalTranscript = paveCanonicalTranscript
                ),
                transcriptImpacts = paveTranscriptImpacts
            )
        )

        every { paveLite.run(GENE, paveCanonicalTranscript, POSITION) } returns PAVE_LITE_ANNOTATION
        paveTranscriptImpacts
            .map(PaveTranscriptImpact::transcript)
            .forEach { transcript ->
                every { paveLite.run(GENE, transcript, POSITION) } returns PAVE_LITE_ANNOTATION
            }
    }

    private fun minimalPaveImpact() = PaveImpact(
        gene = "",
        canonicalTranscript = "",
        canonicalEffects = emptyList(),
        canonicalCodingEffect = PaveCodingEffect.NONE,
        spliceRegion = false,
        hgvsCodingImpact = "",
        hgvsProteinImpact = "",
        otherReportableEffects = null,
        worstCodingEffect = PaveCodingEffect.NONE,
        genesAffected = 1
    )

    private fun paveTranscriptImpact(
        geneId: String = GENE_ID,
        gene: String = GENE,
        transcript: String = OTHER_TRANSCRIPT,
        effects: List<PaveVariantEffect> = emptyList(),
        hgvsCodingImpact: String = HGVS_CODING,
        hgvsProteinImpact: String = HGVS_PROTEIN_1LETTER
    ): PaveTranscriptImpact = PaveTranscriptImpact(
        geneId = geneId,
        gene = gene,
        transcript = transcript,
        effects = effects,
        spliceRegion = false,
        hgvsCodingImpact = hgvsCodingImpact,
        hgvsProteinImpact = hgvsProteinImpact
    )

    private fun transcriptVariantImpact(effects: Set<VariantEffect>, codingEffect: CodingEffect): TranscriptVariantImpact =
        TranscriptVariantImpact(
            transcriptId = OTHER_TRANSCRIPT,
            hgvsCodingImpact = HGVS_CODING,
            hgvsProteinImpact = HGVS_PROTEIN_1LETTER,
            affectedCodon = 1,
            affectedExon = 1,
            inSpliceRegion = false,
            effects = effects,
            codingEffect = codingEffect
        )
}