package com.hartwig.actin.molecular.paver

import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

private val REF_GENOME_FASTA = resourceOnClasspath("paver/ref_genome/ref_genome.fasta")
private val ENSEMBL_DATA_DIR = resourceOnClasspath("paver/ensembl")
private val DRIVER_GENE_PANEL = resourceOnClasspath("paver/DriverGenePanel.tsv")

class PaverTest {

    @get:Rule
    val tempDir = TemporaryFolder()

    @Test
    fun `Should annotate all variants with Paver`() {
        val queries = listOf(
            PaveQuery(
                id = "1",
                chromosome = "1",
                position = 14,
                ref = "A",
                alt = "C",
            ),
            PaveQuery(
                id = "2",
                chromosome = "1",
                position = 26,
                ref = "A",
                alt = "G",
            )
        )

        val paver = Paver(ENSEMBL_DATA_DIR, REF_GENOME_FASTA, PaveRefGenomeVersion.V37, DRIVER_GENE_PANEL, tempDir.root.absolutePath)

        val responses = paver.run(queries).associateBy { it.id }
        assertThat(responses.size).isEqualTo(2)
        val response = responses["1"]!!

        assertThat(response).isEqualTo(
            PaveResponse(
                id = "1",
                impact = PaveImpact(
                    gene = "gene1",
                    canonicalTranscript = "trans1",
                    canonicalEffects = listOf(PaveVariantEffect.MISSENSE),
                    canonicalCodingEffect = PaveCodingEffect.MISSENSE,
                    spliceRegion = false,
                    hgvsCodingImpact = "c.6A>C",
                    hgvsProteinImpact = "p.Lys2Asn",
                    otherReportableEffects = null,
                    worstCodingEffect = PaveCodingEffect.MISSENSE,
                    genesAffected = 1
                ),
                transcriptImpacts = listOf(PaveTranscriptImpact(
                    geneId = "gene_id1",
                    gene = "gene1",
                    transcript = "trans1",
                    effects = listOf(PaveVariantEffect.MISSENSE),
                    spliceRegion = false,
                    hgvsCodingImpact = "c.6A>C",
                    hgvsProteinImpact = "p.Lys2Asn",
                    refSeqId = "",
                    exon = "1",
                    codon = "2")
                )
            )
        )

        val response2 = responses["2"]!!
        assertThat(response2).isEqualTo(
            PaveResponse(
                id = "2",
                impact = PaveImpact(
                    gene = "gene1",
                    canonicalTranscript = "trans1",
                    canonicalEffects = listOf(PaveVariantEffect.SPLICE_DONOR, PaveVariantEffect.SYNONYMOUS),
                    canonicalCodingEffect = PaveCodingEffect.SPLICE,
                    spliceRegion = true,
                    hgvsCodingImpact = "c.18A>G",
                    hgvsProteinImpact = "p.?",
                    otherReportableEffects = null,
                    worstCodingEffect = PaveCodingEffect.SPLICE,
                    genesAffected = 1
                ),
                transcriptImpacts = listOf(PaveTranscriptImpact(
                    geneId = "gene_id1",
                    gene = "gene1",
                    transcript = "trans1",
                    effects = listOf(PaveVariantEffect.SPLICE_DONOR, PaveVariantEffect.SYNONYMOUS),
                    spliceRegion = true,
                    hgvsCodingImpact = "c.18A>G",
                    hgvsProteinImpact = "p.?",
                    refSeqId = "",
                    exon = "1",
                    codon = "6")
                )
            )
        )
    }

    @Test
    fun `Should error if missing PAVE impact field`() {
        assertThatThrownBy { parsePaveImpact(emptyList()) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("Missing PAVE impact field")

        assertThatThrownBy { parsePaveImpact(null) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("Missing PAVE impact field")
    }

    @Test
    fun `Should parse PAVE impact field`() {
        val parsed = parsePaveImpact(listOf(
            "gene_id", "transcript", "missense_variant&inframe_deletion", "MISSENSE", "false", "c.coding", "p.protein",
            "other_reportable_effects", "MISSENSE", "1",
        ))

        assertThat(parsed).isEqualTo(
            PaveImpact(
                gene = "gene_id",
                canonicalTranscript = "transcript",
                canonicalEffects = listOf(PaveVariantEffect.MISSENSE, PaveVariantEffect.INFRAME_DELETION),
                canonicalCodingEffect = PaveCodingEffect.MISSENSE,
                spliceRegion = false,
                hgvsCodingImpact = "c.coding",
                hgvsProteinImpact = "p.protein",
                otherReportableEffects = "other_reportable_effects",
                worstCodingEffect = PaveCodingEffect.MISSENSE,
                genesAffected = 1
            ))
    }

    @Test
    fun `Should error if missing PAVE transcript impact field`() {
        assertThatThrownBy { parsePaveTranscriptImpact(emptyList()) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("Missing PAVE_TI field")

        assertThatThrownBy { parsePaveTranscriptImpact(null) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("Missing PAVE_TI field")
    }

    @Test
    fun `Should parse PAVE Transcript Impact field`() {
        val parsed = parsePaveTranscriptImpact(listOf(
            "gene_id|gene_name|transcript|frameshift_variant&stop_gained|false|c.coding|p.protein|refseq_transcript|1|1",
        ))

        assertThat(parsed).isEqualTo(listOf(
            PaveTranscriptImpact(
                geneId = "gene_id",
                gene = "gene_name",
                transcript = "transcript",
                effects = listOf(PaveVariantEffect.FRAMESHIFT, PaveVariantEffect.STOP_GAINED),
                spliceRegion = false,
                hgvsCodingImpact = "c.coding",
                hgvsProteinImpact = "p.protein",
                refSeqId = "refseq_transcript",
                exon = "1",
                codon = "1",
            )
        ))
    }

    @Test
    fun `Should convert chromosome to numeric index`() {
        assertThat(chromToIndex("1")).isEqualTo(1)
        assertThat(chromToIndex("chr1")).isEqualTo(1)
        assertThat(chromToIndex("X")).isEqualTo(23)
    }
}