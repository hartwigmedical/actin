package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.datamodel.clinical.SequencedAmplification
import com.hartwig.actin.datamodel.clinical.SequencedDeletion
import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.clinical.SequencedSkippedExons
import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.clinical.provided.ProvidedMolecularTestResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val GENE = "gene"
private const val CODING = "coding"
private const val PROTEIN = "protein"
private const val TRANSCRIPT = "transcript"
private const val EXON = 1
private const val CODON = 1
private const val VAF = 0.5
private const val OTHER_GENE = "other gene"
private const val OTHER_TRANSCRIPT = "other transcript"
private const val OTHER_EXON = EXON + 1

class StandardSequencingTestExtractorFunctionsTest {

    @Test
    fun `Should extract variants`() {
        val test = setOf(
            ProvidedMolecularTestResult(
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
        val test = setOf(
            ProvidedMolecularTestResult(
                hgvsCodingImpact = CODING,
            )
        )
        StandardSequencingTestExtractorFunctions.variants(test)
    }

    @Test
    fun `Should extract amplifications`() {
        val test = setOf(
            ProvidedMolecularTestResult(
                gene = GENE,
                amplifiedGene = GENE
            )
        )
        assertThat(StandardSequencingTestExtractorFunctions.amplifications(test)).containsExactly(SequencedAmplification(gene = GENE))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception if gene does not equal amplifiedGene`() {
        val test = setOf(
            ProvidedMolecularTestResult(
                gene = GENE,
                amplifiedGene = "Other gene"
            )
        )
        StandardSequencingTestExtractorFunctions.amplifications(test)
    }

    @Test
    fun `Should extract deletions`() {
        val test = setOf(
            ProvidedMolecularTestResult(
                gene = GENE,
                deletedGene = GENE
            )
        )
        assertThat(StandardSequencingTestExtractorFunctions.deletions(test)).containsExactly(SequencedDeletion(gene = GENE))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception if gene does not equal deletedGene`() {
        val test = setOf(
            ProvidedMolecularTestResult(
                gene = GENE,
                deletedGene = OTHER_GENE
            )
        )
        StandardSequencingTestExtractorFunctions.deletions(test)
    }

    @Test
    fun `Should extract fusions`() {
        val test = setOf(
            ProvidedMolecularTestResult(
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
        val test = setOf(
            ProvidedMolecularTestResult(
                fusionGeneUp = GENE,
                fusionGeneDown = OTHER_GENE
            )
        )
        assertThat(StandardSequencingTestExtractorFunctions.fusions(test)).containsExactly(
            SequencedFusion(
                geneUp = GENE,
                geneDown = OTHER_GENE
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception if gene does not equal any of the up or down genes`() {
        val test = setOf(
            ProvidedMolecularTestResult(
                gene = GENE,
                fusionGeneUp = OTHER_GENE,
                fusionGeneDown = "And another gene"
            )
        )
        StandardSequencingTestExtractorFunctions.fusions(test)
    }

    @Test
    fun `Should extract skipped exons`() {
        val test = setOf(
            ProvidedMolecularTestResult(
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
}