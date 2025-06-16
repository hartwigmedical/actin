package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.config.SequencingTestResultConfig
import com.hartwig.actin.datamodel.clinical.SequencedAmplification
import com.hartwig.actin.datamodel.clinical.SequencedDeletion
import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.clinical.SequencedSkippedExons
import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.clinical.SequencedVirus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val GENE = "gene"
private const val CODING = "coding"
private const val PROTEIN = "protein"
private const val TRANSCRIPT = "transcript"
private const val EXON = 1
private const val CODON = 9
private const val VAF = 0.5
private const val OTHER_GENE = "other gene"
private const val OTHER_TRANSCRIPT = "other transcript"
private const val OTHER_EXON = EXON + 1

class StandardSequencingTestExtractorFunctionsTest {

    @Test
    fun `Should extract variants`() {
        val test = setOf(
            SequencingTestResultConfig(
                input = "",
                gene = GENE,
                transcript = TRANSCRIPT,
                hgvsCodingImpact = CODING,
                hgvsProteinImpact = PROTEIN,
                codon = CODON,
                exon = EXON,
                vaf = VAF
            )
        )
        assertThat(StandardSequencingTestExtractorFunctions.variants(test)).containsExactly(
            SequencedVariant(
                gene = GENE,
                hgvsCodingImpact = CODING,
                hgvsProteinImpact = PROTEIN,
                transcript = TRANSCRIPT,
                codon = CODON,
                exon = EXON,
                variantAlleleFrequency = VAF
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception if coding is known but gene is not`() {
        val test = setOf(SequencingTestResultConfig(input = "", hgvsCodingImpact = CODING))
        StandardSequencingTestExtractorFunctions.variants(test)
    }

    @Test
    fun `Should extract amplifications without copy nr`() {
        val test = setOf(SequencingTestResultConfig(input = "", gene = GENE, amplifiedGene = GENE))
        assertThat(StandardSequencingTestExtractorFunctions.amplifications(test)).containsExactly(SequencedAmplification(gene = GENE))
    }

    @Test
    fun `Should extract amplifications with copy nr`() {
        val test = setOf(SequencingTestResultConfig(input = "", gene = GENE, amplifiedGene = GENE, amplifiedGeneCopyNr = 5))
        assertThat(StandardSequencingTestExtractorFunctions.amplifications(test)).containsExactly(
            SequencedAmplification(
                gene = GENE,
                copies = 5
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception if gene does not equal amplifiedGene`() {
        val test = setOf(SequencingTestResultConfig(input = "", gene = GENE, amplifiedGene = OTHER_GENE))
        StandardSequencingTestExtractorFunctions.amplifications(test)
    }

    @Test
    fun `Should extract deletions`() {
        val test = setOf(SequencingTestResultConfig(input = "", gene = GENE, deletedGene = GENE))
        assertThat(StandardSequencingTestExtractorFunctions.deletions(test)).containsExactly(SequencedDeletion(gene = GENE))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception if gene does not equal deletedGene`() {
        val test = setOf(SequencingTestResultConfig(input = "", gene = GENE, deletedGene = OTHER_GENE))
        StandardSequencingTestExtractorFunctions.deletions(test)
    }

    @Test
    fun `Should extract fusions`() {
        val test = setOf(
            SequencingTestResultConfig(
                input = "",
                gene = GENE,
                fusionGeneUp = GENE,
                fusionGeneDown = OTHER_GENE,
                fusionTranscriptUp = TRANSCRIPT,
                fusionTranscriptDown = OTHER_TRANSCRIPT,
                fusionExonUp = EXON,
                fusionExonDown = OTHER_EXON
            )
        )
        assertThat(StandardSequencingTestExtractorFunctions.fusions(test)).containsExactly(
            SequencedFusion(
                geneUp = GENE,
                geneDown = OTHER_GENE,
                transcriptUp = TRANSCRIPT,
                transcriptDown = OTHER_TRANSCRIPT,
                exonUp = EXON,
                exonDown = OTHER_EXON
            )
        )
    }

    @Test
    fun `Should extract fusions also if gene is null`() {
        val test = setOf(SequencingTestResultConfig(input = "", fusionGeneUp = GENE, fusionGeneDown = OTHER_GENE))
        assertThat(StandardSequencingTestExtractorFunctions.fusions(test)).containsExactly(
            SequencedFusion(
                geneUp = GENE,
                geneDown = OTHER_GENE
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception if gene does not equal any of the up or down genes`() {
        val test =
            setOf(SequencingTestResultConfig(input = "", gene = GENE, fusionGeneUp = OTHER_GENE, fusionGeneDown = "And another gene"))
        StandardSequencingTestExtractorFunctions.fusions(test)
    }

    @Test
    fun `Should extract skipped exons`() {
        val test = setOf(
            SequencingTestResultConfig(
                input = "",
                gene = GENE,
                exonSkipStart = EXON,
                exonSkipEnd = OTHER_EXON,
                transcript = TRANSCRIPT
            )
        )
        assertThat(StandardSequencingTestExtractorFunctions.skippedExons(test)).containsExactly(
            SequencedSkippedExons(
                gene = GENE,
                exonStart = EXON,
                exonEnd = OTHER_EXON,
                transcript = TRANSCRIPT
            )
        )
    }

    @Test
    fun `Should extract virus when integrations is not set`() {
        val test = setOf(SequencingTestResultConfig(input = "HPV", virus = "HPV"))
        assertThat(StandardSequencingTestExtractorFunctions.viruses(test)).containsExactly(
            SequencedVirus(
                virus = "HPV",
                integratedVirus = null
            )
        )
    }

    @Test
    fun `Should extract virus with integrations when integrations is set`() {
        val test = setOf(SequencingTestResultConfig(input = "HPV integrated", virus = "HPV", virusIntegrated = true))
        assertThat(StandardSequencingTestExtractorFunctions.viruses(test)).containsExactly(
            SequencedVirus(
                virus = "HPV",
                integratedVirus = true
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception when interpreting non-ingestable virus`() {
        val test = setOf(SequencingTestResultConfig(input = "", virus = "HPPV"))
        StandardSequencingTestExtractorFunctions.viruses(test)
    }

    @Test
    fun `Should extract tmb`() {
        val test = setOf(
            SequencingTestResultConfig(
                input = "",
                tmb = 5.2
            )
        )
        assertThat(StandardSequencingTestExtractorFunctions.tmb(test)).isEqualTo(5.2)
    }

    @Test
    fun `Should extract msi`() {
        val test = setOf(
            SequencingTestResultConfig(
                input = "",
                msi = true
            )
        )
        assertThat(StandardSequencingTestExtractorFunctions.msi(test)).isEqualTo(true)
    }

    @Test
    fun `Should extract hrd`() {
        val test = setOf(
            SequencingTestResultConfig(
                input = "",
                hrd = false
            )
        )
        assertThat(StandardSequencingTestExtractorFunctions.hrd(test)).isEqualTo(false)
    }
}