package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestVariantAlterationFactory
import com.hartwig.actin.datamodel.molecular.driver.TranscriptVariantImpact
import com.hartwig.actin.datamodel.molecular.driver.VariantEffect
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevelDetails
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestEvidenceDirectionFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestTreatmentEvidenceFactory
import com.hartwig.actin.molecular.driverlikelihood.DndsDatabase
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.driverlikelihood.TEST_ONCO_DNDS_TSV
import com.hartwig.actin.molecular.driverlikelihood.TEST_TSG_DNDS_TSV
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
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
private const val HGVS_PROTEIN_3LETTER = "p.Met1Leu"
private const val HGVS_PROTEIN_1LETTER = "p.M1L"
private val ARCHER_VARIANT = SequencedVariant(gene = GENE, hgvsCodingImpact = HGVS_CODING)

private val VARIANT_MATCH_CRITERIA =
    VariantMatchCriteria(
        gene = GENE,
        codingEffect = CodingEffect.MISSENSE,
        type = VariantType.SNV,
        chromosome = CHROMOSOME,
        position = POSITION,
        ref = REF,
        alt = ALT,
        driverLikelihood = DriverLikelihood.HIGH,
        isReportable = true,
    )

private val EMPTY_MATCH = TestClinicalEvidenceFactory.createEmpty()
private val ACTIONABILITY_MATCH = TestClinicalEvidenceFactory.withEvidence(
    TestTreatmentEvidenceFactory.create(
        treatment = "treatment",
        isOnLabel = true,
        evidenceLevel = EvidenceLevel.A,
        evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
        evidenceDirection = TestEvidenceDirectionFactory.certainPositiveResponse(),
    )
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

private val HOTSPOT_CKB =
    TestVariantAlterationFactory.createVariantAlteration(GENE, GeneRole.ONCO, ProteinEffect.GAIN_OF_FUNCTION, true, true)

private val HOTSPOT_DOCM = TestVariantAlterationFactory.createVariantAlteration(GENE, isHotspot = true)

private val NON_HOTSPOT =
    TestVariantAlterationFactory.createVariantAlteration(GENE, GeneRole.ONCO, ProteinEffect.NO_EFFECT, false, false)

class PanelVariantAnnotatorTest {

    private val evidenceDatabase = mockk<EvidenceDatabase> {
        every { variantAlterationForVariant(any()) } returns TestVariantAlterationFactory.createVariantAlteration(GENE)
        every { variantAlterationForVariant(VARIANT_MATCH_CRITERIA.copy(driverLikelihood = null)) } returns HOTSPOT_CKB
        every { evidenceForVariant(any()) } returns EMPTY_MATCH
        every { evidenceForVariant(VARIANT_MATCH_CRITERIA) } returns ACTIONABILITY_MATCH
    }
    private val geneDriverLikelihoodModel = GeneDriverLikelihoodModel(DndsDatabase.create(TEST_ONCO_DNDS_TSV, TEST_TSG_DNDS_TSV))
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
    private val annotator = PanelVariantAnnotator(evidenceDatabase, geneDriverLikelihoodModel, transvarAnnotator, paver, paveLite)

    @Test
    fun `Should annotate variants with transcript, genetic variation and genomic position`() {
        val annotated = annotator.annotate(setOf(ARCHER_VARIANT)).first()
        assertThat(annotated.variantAlleleFrequency).isNull()
        assertThat(annotated.canonicalImpact.transcriptId).isEqualTo(TRANSCRIPT)
        assertThat(annotated.canonicalImpact.hgvsCodingImpact).isEqualTo(HGVS_CODING)
        assertThat(annotated.canonicalImpact.codingEffect).isEqualTo(CodingEffect.MISSENSE)
        assertThat(annotated.canonicalImpact.hgvsProteinImpact).isEqualTo(HGVS_PROTEIN_1LETTER)
        assertThat(annotated.canonicalImpact.isSpliceRegion).isFalse()
        assertThat(annotated.otherImpacts).isEmpty()
        assertThat(annotated.chromosome).isEqualTo(CHROMOSOME)
        assertThat(annotated.position).isEqualTo(POSITION)
        assertThat(annotated.ref).isEqualTo(REF)
        assertThat(annotated.alt).isEqualTo(ALT)
        assertThat(annotated.type).isEqualTo(VariantType.SNV)
    }

    @Test
    fun `Should annotate variants with gene alteration data`() {
        val annotated = annotator.annotate(setOf(ARCHER_VARIANT)).first()
        assertThat(annotated.geneRole).isEqualTo(GeneRole.ONCO)
        assertThat(annotated.isAssociatedWithDrugResistance).isTrue()
    }

    @Test
    fun `Should annotate variant that is hotspot`() {
        val evidenceDatabase = mockk<EvidenceDatabase> {
            every { variantAlterationForVariant(any()) } returns TestVariantAlterationFactory.createVariantAlteration(GENE)
            every { variantAlterationForVariant(VARIANT_MATCH_CRITERIA.copy(driverLikelihood = null)) } returns HOTSPOT_DOCM
            every { evidenceForVariant(any()) } returns EMPTY_MATCH
            every { evidenceForVariant(VARIANT_MATCH_CRITERIA) } returns ACTIONABILITY_MATCH
        }
        val annotator = PanelVariantAnnotator(evidenceDatabase, geneDriverLikelihoodModel, transvarAnnotator, paver, paveLite)
        val annotated = annotator.annotate(setOf(ARCHER_VARIANT)).first()
        assertThat(annotated.isHotspot).isTrue()
        assertThat(annotated.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
    }

    @Test
    fun `Should annotate variant that is no hotspot`() {
        val evidenceDatabase = mockk<EvidenceDatabase> {
            every { variantAlterationForVariant(any()) } returns TestVariantAlterationFactory.createVariantAlteration(GENE)
            every { variantAlterationForVariant(VARIANT_MATCH_CRITERIA.copy(driverLikelihood = null)) } returns NON_HOTSPOT
            every { evidenceForVariant(any()) } returns EMPTY_MATCH
            every { evidenceForVariant(VARIANT_MATCH_CRITERIA) } returns ACTIONABILITY_MATCH
        }
        val annotator = PanelVariantAnnotator(evidenceDatabase, geneDriverLikelihoodModel, transvarAnnotator, paver, paveLite)
        val annotated = annotator.annotate(setOf(ARCHER_VARIANT)).first()
        assertThat(annotated.isHotspot).isFalse()
        assertThat(annotated.driverLikelihood).isNull()
    }

    @Test
    fun `Should not annotate with evidence when no matches found`() {
        val annotated = annotator.annotate(setOf(ARCHER_VARIANT.copy(gene = "other gene"))).first()
        assertThat(annotated.evidence).isEqualTo(TestClinicalEvidenceFactory.createEmpty())
    }

    @Test
    fun `Should annotate variants with evidence when matches found`() {
        val annotated = annotator.annotate(setOf(ARCHER_VARIANT)).first()

        assertThat(annotated.evidence).isEqualTo(
            ClinicalEvidence(
                treatmentEvidence = setOf(
                    TestTreatmentEvidenceFactory.create(
                        treatment = "treatment",
                        isOnLabel = true,
                        evidenceLevel = EvidenceLevel.A,
                        evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
                        evidenceDirection = TestEvidenceDirectionFactory.certainPositiveResponse(),
                    )
                ),
                eligibleTrials = emptySet()
            )
        )
    }

    @Test
    fun `Should filter variant on null output from transcript annotator`() {
        every { transvarAnnotator.resolve(GENE, null, HGVS_CODING) } returns null
        assertThat(annotator.annotate(setOf(ARCHER_VARIANT))).isEmpty()
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
            transcriptImpact = listOf(
                paveTranscriptImpact(OTHER_GENE_ID, OTHER_GENE, OTHER_GENE_TRANSCRIPT)
            )
        )

        val transcriptImpact = annotator.otherImpacts(complexPaveAnnotation, TRANSCRIPT_ANNOTATION)
        assertThat(transcriptImpact).isEqualTo(emptySet<TranscriptVariantImpact>())
    }

    @Test
    fun `Should annotate valid other transcripts with paveLite`() {
        val complexPaveAnnotation = PAVE_ANNOTATION.copy(transcriptImpact = listOf(paveTranscriptImpact()))

        every { paveLite.run(GENE, OTHER_TRANSCRIPT, POSITION) } returns PAVE_LITE_ANNOTATION

        val transcriptImpact = annotator.otherImpacts(complexPaveAnnotation, TRANSCRIPT_ANNOTATION)
        assertThat(transcriptImpact).containsExactly(transcriptVariantImpact(emptySet(), CodingEffect.NONE))
    }

    @Test
    fun `Should retain all effects data and have complete annotation of variants`() {
        val complexPaveAnnotation = PAVE_ANNOTATION.copy(
            transcriptImpact = listOf(
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
                        canonicalEffect = "upstream_gene_variant",
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

    private fun paveTranscriptImpact(
        geneId: String = GENE_ID,
        gene: String = GENE,
        transcript: String = OTHER_TRANSCRIPT,
        effects: List<PaveVariantEffect> = emptyList()
    ): PaveTranscriptImpact = PaveTranscriptImpact(
        geneId = geneId,
        gene = gene,
        transcript = transcript,
        effects = effects,
        spliceRegion = false,
        hgvsCodingImpact = HGVS_CODING,
        hgvsProteinImpact = HGVS_PROTEIN_3LETTER
    )

    private fun transcriptVariantImpact(
        effects: Set<VariantEffect>,
        codingEffect: CodingEffect
    ): TranscriptVariantImpact = TranscriptVariantImpact(
        transcriptId = OTHER_TRANSCRIPT,
        hgvsCodingImpact = HGVS_CODING,
        hgvsProteinImpact = HGVS_PROTEIN_1LETTER,
        affectedCodon = 1,
        affectedExon = 1,
        isSpliceRegion = false,
        effects = effects,
        codingEffect = codingEffect
    )
}