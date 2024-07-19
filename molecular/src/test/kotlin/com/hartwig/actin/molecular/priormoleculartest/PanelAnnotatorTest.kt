package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.panel.PanelVariantExtraction
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.actin.molecular.evidence.actionability.TestServeActionabilityFactory
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.actin.molecular.paver.PaveCodingEffect
import com.hartwig.actin.molecular.paver.PaveImpact
import com.hartwig.actin.molecular.paver.PaveQuery
import com.hartwig.actin.molecular.paver.PaveResponse
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.tools.pave.ImmutableVariantTranscriptImpact
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.actin.tools.variant.ImmutableVariant
import com.hartwig.actin.tools.variant.VariantAnnotator
import com.hartwig.serve.datamodel.EvidenceDirection
import com.hartwig.serve.datamodel.EvidenceLevel
import com.hartwig.serve.datamodel.Knowledgebase
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val ALT = "T"
private const val REF = "G"
private const val TRANSCRIPT = "transcript"
private const val CHROMOSOME = "1"
private const val POSITION = 1
private val EMPTY_MATCH = ActionabilityMatch(emptyList(), emptyList())
private val ARCHER_VARIANT = PanelVariantExtraction(GENE, HGVS_CODING)
private val ARCHER_PANEL_WITH_VARIANT = ArcherPanelExtraction(variants = listOf(ARCHER_VARIANT))
private val VARIANT_MATCH_CRITERIA =
    VariantMatchCriteria(isReportable = true, gene = GENE, chromosome = CHROMOSOME, ref = REF, alt = ALT, position = POSITION)
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

class PanelAnnotatorTest {

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

    private val annotator = PanelAnnotator(ExperimentType.ARCHER, evidenceDatabase, geneDriverLikelihoodModel, transvarAnnotator, paver, paveLite)

    @Test
    fun `Should return empty annotation when no matches found`() {
        val annotated = annotator.annotate(ARCHER_PANEL_WITH_VARIANT)
        assertThat(annotated.drivers.variants.first().evidence).isEqualTo(ActionableEvidence())
    }

    @Test
    fun `Should annotate variants with evidence`() {
        every { evidenceDatabase.evidenceForVariant(VARIANT_MATCH_CRITERIA) } returns ActionabilityMatch(
            onLabelEvents = listOf(
                TestServeActionabilityFactory.geneBuilder().build().withSource(Knowledgebase.CKB_EVIDENCE).withLevel(EvidenceLevel.A)
                    .withDirection(EvidenceDirection.RESPONSIVE)
            ), offLabelEvents = emptyList()
        )
        val annotated = annotator.annotate(ARCHER_PANEL_WITH_VARIANT)
        assertThat(annotated.drivers.variants.first().evidence).isEqualTo(ActionableEvidence(approvedTreatments = setOf("")))
    }

    @Test
    fun `Should annotate variants with gene alteration`() {
        setupGeneAlteration()
        val annotated = annotator.annotate(ARCHER_PANEL_WITH_VARIANT)
        assertThat(annotated.drivers.variants.first().geneRole).isEqualTo(GeneRole.ONCO)
        assertThat(annotated.drivers.variants.first().proteinEffect).isEqualTo(ProteinEffect.GAIN_OF_FUNCTION)
    }

    @Test
    fun `Should annotate variants with driver likelihood`() {
        setupGeneAlteration()
        every { geneDriverLikelihoodModel.evaluate(GENE, GeneRole.ONCO, any()) } returns 0.9
        val annotated = annotator.annotate(ARCHER_PANEL_WITH_VARIANT)
        assertThat(annotated.drivers.variants.first().driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
    }

    @Test
    fun `Should annotate variants with transcript, genetic variation and genomic position`() {
        setupGeneAlteration()
        val annotated = annotator.annotate(ARCHER_PANEL_WITH_VARIANT)
        val annotatedVariant = annotated.drivers.variants.first()
        assertThat(annotatedVariant.canonicalImpact.transcriptId).isEqualTo(TRANSCRIPT)
        assertThat(annotatedVariant.canonicalImpact.hgvsCodingImpact).isEqualTo(HGVS_CODING)
        assertThat(annotatedVariant.canonicalImpact.codingEffect).isEqualTo(com.hartwig.actin.molecular.datamodel.CodingEffect.MISSENSE)
        assertThat(annotatedVariant.canonicalImpact.hgvsProteinImpact).isEqualTo(HGVS_PROTEIN)
        assertThat(annotatedVariant.chromosome).isEqualTo(CHROMOSOME)
        assertThat(annotatedVariant.position).isEqualTo(POSITION)
        assertThat(annotatedVariant.ref).isEqualTo(REF)
        assertThat(annotatedVariant.alt).isEqualTo(ALT)
        assertThat(annotatedVariant.type).isEqualTo(com.hartwig.actin.molecular.datamodel.VariantType.SNV)
    }

    @Test
    fun `Should filter variant on null output from transcript annotator`() {
        every { transvarAnnotator.resolve(GENE, null, HGVS_CODING) } returns null
        assertThat(annotator.annotate(ARCHER_PANEL_WITH_VARIANT).drivers.variants).isEmpty()
    }

    @Test
    fun `Should annotate variants with affected exon and codon`() {
        setupGeneAlteration()
        every { paveLite.run(GENE, TRANSCRIPT, POSITION) } returns ImmutableVariantTranscriptImpact.builder().affectedExon(1)
            .affectedCodon(2).build()
        val annotated = annotator.annotate(ARCHER_PANEL_WITH_VARIANT)
        val annotatedVariant = annotated.drivers.variants.first()
        assertThat(annotatedVariant.canonicalImpact.affectedExon).isEqualTo(1)
        assertThat(annotatedVariant.canonicalImpact.affectedCodon).isEqualTo(2)
    }

    private fun setupGeneAlteration() {
        every { evidenceDatabase.geneAlterationForVariant(VARIANT_MATCH_CRITERIA) } returns TestServeKnownFactory.hotspotBuilder().build()
            .withGeneRole(com.hartwig.serve.datamodel.common.GeneRole.ONCO)
            .withProteinEffect(com.hartwig.serve.datamodel.common.ProteinEffect.GAIN_OF_FUNCTION)
    }
}