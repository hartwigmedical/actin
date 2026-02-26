package com.hartwig.actin.molecular.panel

import com.hartwig.actin.molecular.paver.PaveCodingEffect
import com.hartwig.actin.molecular.paver.PaveImpact
import com.hartwig.actin.molecular.paver.PaveResponse
import com.hartwig.actin.molecular.paver.PaveTranscriptImpact
import com.hartwig.actin.molecular.paver.PaveVariantEffect
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PhaseSetExonCodonResolverTest {

    @Test
    fun `Should keep matching exon and codon across phase set`() {
        val responses = listOf(
            paveResponse(exon = 10, codon = 20),
            paveResponse(exon = 10, codon = 20)
        )

        val resolved = PhaseSetExonCodonResolver.resolve(1, responses)

        assertThat(resolved).isEqualTo(ExonCodon(10, 20))
    }

    @Test
    fun `Should select min exon and codon when mismatch`() {
        val responses = listOf(
            paveResponse(exon = 5, codon = 50),
            paveResponse(exon = 10, codon = 10)
        )

        val resolved = PhaseSetExonCodonResolver.resolve(2, responses)
        assertThat(resolved).isEqualTo(ExonCodon(10, 10))

        val adjusted = PhaseSetExonCodonResolver.applyToResponse(responses.first(), resolved!!)
        val canonicalImpact = adjusted.transcriptImpacts.single { it.transcript == adjusted.impact.canonicalTranscript }
        assertThat(canonicalImpact.exon).isEqualTo(10)
        assertThat(canonicalImpact.codon).isEqualTo(10)
    }

    private fun paveResponse(
        exon: Int,
        codon: Int,
        transcript: String = "TX"
    ): PaveResponse {
        val impact = PaveImpact(
            gene = "GENE",
            canonicalTranscript = transcript,
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
            transcript = transcript,
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

    @Test
    fun `Should throw when canonical transcript differs across phase set`() {
        val responses = listOf(
            paveResponse(exon = 10, codon = 20, transcript = "TX1"),
            paveResponse(exon = 11, codon = 21, transcript = "TX2")
        )

        val exception = org.assertj.core.api.Assertions.catchThrowable {
            PhaseSetExonCodonResolver.resolve(3, responses)
        }

        assertThat(exception).isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("mismatched canonical transcripts")
    }
}
