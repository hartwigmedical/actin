package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.molecular.CodingEffect
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.GeneRole
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.TranscriptImpact
import com.hartwig.actin.datamodel.molecular.VariantType
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceDirection
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory.treatment
import com.hartwig.actin.molecular.GENE
import com.hartwig.actin.molecular.HGVS_CODING
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import com.hartwig.actin.molecular.evidence.matching.EvidenceDatabase
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
import com.hartwig.serve.datamodel.Knowledgebase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.hartwig.serve.datamodel.EvidenceDirection as ServeEvidenceDirection
import com.hartwig.serve.datamodel.EvidenceLevel as ServeEvidenceLevel


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
private const val HGVS_PROTEIN_3LETTER = "p.Met1Leu"
private const val HGVS_PROTEIN_1LETTER = "p.M1L"
private val EMPTY_MATCH = ActionabilityMatch(emptyList(), emptyList())
private val ARCHER_VARIANT = SequencedVariant(gene = GENE, hgvsCodingImpact = HGVS_CODING)

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

private val ACTIONABILITY_MATCH = ActionabilityMatch(
    onLabelEvents = listOf(
        TestServeActionabilityFactory.geneBuilder().build().withSource(Knowledgebase.CKB_EVIDENCE).withLevel(ServeEvidenceLevel.A)
            .withDirection(ServeEvidenceDirection.RESPONSIVE)
    ), offLabelEvents = emptyList()
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
        hgvsProteinImpact = HGVS_PROTEIN_3LETTER,
        otherReportableEffects = null,
        worstCodingEffect = PaveCodingEffect.MISSENSE,
        genesAffected = 1
    ),
    transcriptImpact = emptyList()
)

private val HOTSPOT = TestServeKnownFactory.hotspotBuilder().build()
    .withGeneRole(com.hartwig.serve.datamodel.common.GeneRole.ONCO)
    .withProteinEffect(com.hartwig.serve.datamodel.common.ProteinEffect.GAIN_OF_FUNCTION)

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
    fun `Should return empty annotation when no matches found`() {
        val annotated = annotator.annotate(setOf(ARCHER_VARIANT))
        assertThat(annotated.first().evidence).isEqualTo(ClinicalEvidence())
    }

    @Test
    fun `Should annotate variants with evidence`() {
        every { evidenceDatabase.evidenceForVariant(VARIANT_MATCH_CRITERIA) } returns ACTIONABILITY_MATCH
        val annotated = annotator.annotate(setOf(ARCHER_VARIANT))
        assertThat(annotated.first().evidence).isEqualTo(
            ClinicalEvidence(
                treatmentEvidence = setOf(
                    treatment(
                        treatment = "intervention",
                        evidenceLevel = EvidenceLevel.A,
                        direction = EvidenceDirection(hasPositiveResponse = true, isCertain = true, hasBenefit = true),
                        onLabel = true,
                        isCategoryEvent = true
                    )
                )
            )
        )
    }

    @Test
    fun `Should annotate variants with gene alteration`() {
        setupGeneAlteration()
        val annotated = annotator.annotate(setOf(ARCHER_VARIANT))
        assertThat(annotated.first().geneRole).isEqualTo(GeneRole.ONCO)
        assertThat(annotated.first().proteinEffect).isEqualTo(ProteinEffect.GAIN_OF_FUNCTION)
    }

    @Test
    fun `Should annotate variants with driver likelihood`() {
        setupGeneAlteration()
        every { geneDriverLikelihoodModel.evaluate(GENE, GeneRole.ONCO, any()) } returns 0.9
        val annotated = annotator.annotate(setOf(ARCHER_VARIANT))
        assertThat(annotated.first().driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
    }

    @Test
    fun `Should annotate variants with transcript, genetic variation and genomic position`() {
        setupGeneAlteration()
        val annotated = annotator.annotate(setOf(ARCHER_VARIANT))
        val annotatedVariant = annotated.first()
        assertThat(annotatedVariant.canonicalImpact.transcriptId).isEqualTo(TRANSCRIPT)
        assertThat(annotatedVariant.canonicalImpact.hgvsCodingImpact).isEqualTo(HGVS_CODING)
        assertThat(annotatedVariant.canonicalImpact.codingEffect).isEqualTo(CodingEffect.MISSENSE)
        assertThat(annotatedVariant.canonicalImpact.hgvsProteinImpact).isEqualTo(HGVS_PROTEIN_1LETTER)
        assertThat(annotatedVariant.chromosome).isEqualTo(CHROMOSOME)
        assertThat(annotatedVariant.position).isEqualTo(POSITION)
        assertThat(annotatedVariant.ref).isEqualTo(REF)
        assertThat(annotatedVariant.alt).isEqualTo(ALT)
        assertThat(annotatedVariant.type).isEqualTo(VariantType.SNV)
        assertThat(annotatedVariant.isHotspot).isTrue()
    }

    @Test
    fun `Should filter variant on null output from transcript annotator`() {
        every { transvarAnnotator.resolve(GENE, null, HGVS_CODING) } returns null
        assertThat(annotator.annotate(setOf(ARCHER_VARIANT))).isEmpty()
    }

    @Test
    fun `Should annotate variants with affected exon and codon`() {
        setupGeneAlteration()
        every { paveLite.run(GENE, TRANSCRIPT, POSITION) } returns ImmutableVariantTranscriptImpact.builder().affectedExon(1)
            .affectedCodon(2).build()
        val annotated = annotator.annotate(setOf(ARCHER_VARIANT))
        val annotatedVariant = annotated.first()
        assertThat(annotatedVariant.canonicalImpact.affectedExon).isEqualTo(1)
        assertThat(annotatedVariant.canonicalImpact.affectedCodon).isEqualTo(2)
    }

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
                    hgvsProteinImpact = HGVS_PROTEIN_3LETTER
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
                    hgvsProteinImpact = HGVS_PROTEIN_3LETTER
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
                    hgvsProteinImpact = HGVS_PROTEIN_1LETTER,
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
        val variants = emptySet<SequencedVariant>()
        val annotatedVariants = annotator.annotate(variants)
        assertThat(annotatedVariants).isEmpty()
        verify(exactly = 0) { paver.run(any()) }
    }

    @Test
    fun `Should describe variant event using protein hgvs`() {
        val variants = setOf(SequencedVariant(gene = GENE, hgvsCodingImpact = HGVS_CODING))
        val annotated = annotator.annotate(variants)
        assertThat(annotated.first().event).isEqualTo("$GENE ${HGVS_PROTEIN_1LETTER.removePrefix("p.")}")
    }

    @Test
    fun `Should describe variant using coding hgvs for event when no protein impact`() {
        every { paver.run(listOf(PAVE_QUERY)) } returns listOf(
            PAVE_ANNOTATION.copy(
                impact = PAVE_ANNOTATION.impact.copy(hgvsProteinImpact = "p.?")
            )
        )

        val variants = setOf(SequencedVariant(gene = GENE, hgvsCodingImpact = HGVS_CODING))
        val annotated = annotator.annotate(variants)
        assertThat(annotated.first().event).isEqualTo("$GENE $HGVS_CODING")
    }

    @Test
    fun `Should determine impact from PaveResponse`() {
        assertThat(
            impact(
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
            impact(
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
            impact(
                PAVE_ANNOTATION.copy(
                    impact = minimalPaveImpact().copy(
                        canonicalEffect = "upstream_gene_variant",
                        canonicalCodingEffect = PaveCodingEffect.NONE,
                        hgvsCodingImpact = "",
                        hgvsProteinImpact = "",
                    )
                )
            )
        ).isEqualTo("upstream")

        assertThat(
            impact(
                PAVE_ANNOTATION.copy(
                    impact = minimalPaveImpact().copy(
                        canonicalEffect = "something&another_thing",
                        canonicalCodingEffect = PaveCodingEffect.NONE,
                        hgvsCodingImpact = "",
                        hgvsProteinImpact = "",
                    )
                )
            )
        ).isEqualTo("something&another_thing")
    }

    private fun minimalPaveImpact() = PaveImpact(
        gene = "",
        transcript = "",
        canonicalEffect = "",
        canonicalCodingEffect = PaveCodingEffect.NONE,
        spliceRegion = false,
        hgvsCodingImpact = "",
        hgvsProteinImpact = "",
        otherReportableEffects = null,
        worstCodingEffect = PaveCodingEffect.NONE,
        genesAffected = 1
    )


    private fun setupGeneAlteration() {
        every { evidenceDatabase.geneAlterationForVariant(VARIANT_MATCH_CRITERIA) } returns HOTSPOT
    }
}