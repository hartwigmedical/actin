package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.molecular.paver.PaveCodingEffect
import com.hartwig.actin.molecular.paver.PaveImpact
import com.hartwig.actin.molecular.paver.PaveResponse
import com.hartwig.actin.molecular.paver.PaveTranscriptImpact
import com.hartwig.actin.molecular.paver.PaveVariantEffect
import com.hartwig.actin.tools.variant.ImmutableVariant
import com.hartwig.actin.tools.variant.VariantAnnotator
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PhasedDecompositionCollapserTest {

    private val variantResolver = mockk<VariantAnnotator>()
    private val collapser = PhasedDecompositionCollapser(variantResolver)

    @Test
    fun `Should apply original coordinates and resolved exon codon to representative`() {
        val sequencedVariant = SequencedVariant(gene = "B", transcript = null, hgvsCodingImpact = "c.2A>T")
        val representative = AnnotatableVariant(
            queryId = 0,
            sequencedVariant = sequencedVariant,
            queryHgvs = "c.2A>G",
            localPhaseSet = 1,
            transvarVariant = transvarVariant(position = 2, ref = "A", alt = "G"),
            paveResponse = paveResponse(exon = 5, codon = 50)
        )
        val other = AnnotatableVariant(
            queryId = 1,
            sequencedVariant = sequencedVariant,
            queryHgvs = "c.3_4delinsA",
            localPhaseSet = 1,
            transvarVariant = transvarVariant(position = 3, ref = "AT", alt = "A"),
            paveResponse = paveResponse(exon = 10, codon = 10)
        )

        every { variantResolver.resolve("B", null, "c.2A>T") } returns transvarVariant(position = 99, ref = "C", alt = "T")

        val collapsed = collapser.collapse(1, listOf(representative, other))

        assertThat(collapsed.transvarVariant?.position()).isEqualTo(99)
        assertThat(collapsed.transvarVariant?.ref()).isEqualTo("C")
        assertThat(collapsed.transvarVariant?.alt()).isEqualTo("T")

        val canonicalImpact = collapsed.paveResponse!!.transcriptImpacts
            .single { it.transcript == collapsed.paveResponse!!.impact.canonicalTranscript }
        assertThat(canonicalImpact.exon).isEqualTo(10)
        assertThat(canonicalImpact.codon).isEqualTo(10)
    }

    private fun paveResponse(exon: Int, codon: Int): PaveResponse {
        val impact = PaveImpact(
            gene = "GENE",
            canonicalTranscript = "TX",
            canonicalEffects = listOf(PaveVariantEffect.MISSENSE),
            canonicalCodingEffect = PaveCodingEffect.MISSENSE,
            spliceRegion = false,
            hgvsCodingImpact = "c.mock",
            hgvsProteinImpact = "p.M1L",
            otherReportableEffects = null,
            worstCodingEffect = PaveCodingEffect.MISSENSE,
            genesAffected = 1
        )
        val transcriptImpact = PaveTranscriptImpact(
            geneId = "gene_id",
            gene = "GENE",
            transcript = "TX",
            effects = listOf(PaveVariantEffect.MISSENSE),
            spliceRegion = false,
            hgvsCodingImpact = "c.mock",
            hgvsProteinImpact = "p.M1L",
            refSeqId = "refseq",
            exon = exon,
            codon = codon
        )
        return PaveResponse(
            id = "0",
            impact = impact,
            transcriptImpacts = listOf(transcriptImpact),
            localPhaseSet = 1
        )
    }

    private fun transvarVariant(position: Int, ref: String, alt: String) =
        ImmutableVariant.builder()
            .chromosome("7")
            .position(position)
            .ref(ref)
            .alt(alt)
            .build()
}
