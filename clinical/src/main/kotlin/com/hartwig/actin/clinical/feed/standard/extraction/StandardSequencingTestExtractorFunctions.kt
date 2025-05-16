package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.config.SequencingTestResultConfig
import com.hartwig.actin.datamodel.clinical.SequencedAmplification
import com.hartwig.actin.datamodel.clinical.SequencedDeletion
import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.clinical.SequencedSkippedExons
import com.hartwig.actin.datamodel.clinical.SequencedVariant

object StandardSequencingTestExtractorFunctions {

    fun variants(results: Set<SequencingTestResultConfig>) =
        results.filter { result -> result.hgvsCodingImpact != null || result.hgvsProteinImpact != null }
            .map { result ->
                SequencedVariant(
                    gene = result.gene
                        ?: throw IllegalArgumentException("Gene must be defined when hgvs protein/coding impact are indicated"),
                    hgvsCodingImpact = result.hgvsCodingImpact,
                    hgvsProteinImpact = result.hgvsProteinImpact,
                    transcript = result.transcript,
                    variantAlleleFrequency = result.vaf,
                    exon = result.exon,
                    codon = result.codon
                )
            }.toSet()

    fun amplifications(results: Set<SequencingTestResultConfig>) =
        results.mapNotNull { result ->
            result.amplifiedGene?.let { amplifiedGene ->
                if (configuredGenesAreNotEqual(result.gene, amplifiedGene)) {
                    throw IllegalArgumentException("Gene must be equal to amplifiedGene if both are set.")
                }
                SequencedAmplification(amplifiedGene, result.transcript)
            }
        }.toSet()

    fun deletions(results: Set<SequencingTestResultConfig>) =
        results.mapNotNull { result ->
            result.deletedGene?.let { deletedGene ->
                if (configuredGenesAreNotEqual(result.gene, deletedGene)) {
                    throw IllegalArgumentException("Gene must be equal to deletedGene if both are set.")
                }
                SequencedDeletion(deletedGene, result.transcript)
            }
        }.toSet()


    fun tmb(results: Set<SequencingTestResultConfig>) = results.firstNotNullOfOrNull { result -> result.tmb }

    fun msi(results: Set<SequencingTestResultConfig>) = results.firstNotNullOfOrNull { result -> result.msi }

    fun skippedExons(results: Set<SequencingTestResultConfig>) = results.mapNotNull { result ->
        result.exonSkipStart?.let { exonSkipStart ->
            SequencedSkippedExons(
                gene = result.gene!!,
                exonStart = exonSkipStart,
                exonEnd = result.exonSkipEnd ?: exonSkipStart,
                transcript = result.transcript
            )
        }
    }.toSet()

    fun fusions(results: Set<SequencingTestResultConfig>): Set<SequencedFusion> {
        val fusionResults = results.filter { result -> result.fusionGeneUp != null || result.fusionGeneDown != null }
        if (fusionResults.any {
            configuredGenesAreNotEqual(it.gene, it.fusionGeneUp) && configuredGenesAreNotEqual(it.gene, it.fusionGeneDown)
        }) {
            throw IllegalArgumentException("Gene must be equal to fusionGeneUp or fusionGeneDown if gene is set")
        }
        return fusionResults
            .map { result -> SequencedFusion(
                geneUp = result.fusionGeneUp,
                geneDown = result.fusionGeneDown,
                transcriptUp = result.fusionTranscriptUp,
                transcriptDown = result.fusionTranscriptDown,
                exonUp = result.fusionExonUp,
                exonDown = result.fusionExonDown
            ) }
            .toSet()
    }

    private fun configuredGenesAreNotEqual(gene1: String?, gene2: String?): Boolean {
        return (gene1 != null && gene2 != null && gene1 != gene2)
    }
}