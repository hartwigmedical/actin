package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.datamodel.clinical.SequencedAmplification
import com.hartwig.actin.datamodel.clinical.SequencedDeletion
import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.clinical.SequencedSkippedExons
import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.clinical.provided.ProvidedMolecularTestResult

object StandardSequencingTestExtractorFunctions {

    fun variants(results: Set<ProvidedMolecularTestResult>) =
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
                    codon = result.codon,
                )
            }.toSet()

    fun amplifications(results: Set<ProvidedMolecularTestResult>) =
        results.mapNotNull {
            if (configuredGenesAreNotEqual(it.gene, it.amplifiedGene)) {
                throw IllegalArgumentException("Gene must be equal to amplifiedGene if both are set.")
            } else {
                it.amplifiedGene?.let { gene -> SequencedAmplification(gene, it.transcript) }
            }
        }.toSet()

    fun deletions(results: Set<ProvidedMolecularTestResult>) =
        results.mapNotNull {
            if (configuredGenesAreNotEqual(it.gene, it.deletedGene)) {
                throw IllegalArgumentException("Gene must be equal to deletedGene if both are set.")
            } else {
                it.deletedGene?.let { gene -> SequencedDeletion(gene, it.transcript) }
            }
        }.toSet()

    fun fusions(results: Set<ProvidedMolecularTestResult>): Set<SequencedFusion> {
        results.forEach {
            if (it.gene != null && configuredGenesAreNotEqual(it.gene, it.fusionGeneUp) && (configuredGenesAreNotEqual(
                    it.gene,
                    it.fusionGeneDown
                ))
            ) {
                throw IllegalArgumentException("Gene must be equal to fusionGeneUp or fusionGeneDown if gene is set")
            }
        }
        return results.filter { result -> result.fusionGeneUp != null || result.fusionGeneDown != null }
            .map { result ->
                SequencedFusion(
                    geneUp = result.fusionGeneUp,
                    geneDown = result.fusionGeneDown,
                    transcriptUp = result.fusionTranscriptUp,
                    transcriptDown = result.fusionTranscriptDown,
                    exonUp = result.fusionExonUp,
                    exonDown = result.fusionExonDown
                )
            }.toSet()
    }

    fun skippedExons(
        results: Set<ProvidedMolecularTestResult>
    ) = results.mapNotNull { result ->
        result.exonSkipStart?.let { exonSkipStart ->
            SequencedSkippedExons(
                gene = result.gene!!,
                exonStart = result.exonSkipStart!!,
                exonEnd = result.exonSkipEnd ?: exonSkipStart,
                transcript = result.transcript
            )
        }
    }.toSet()

    fun tmb(results: Set<ProvidedMolecularTestResult>) =
        results.firstNotNullOfOrNull { result -> result.tmb }

    fun msi(results: Set<ProvidedMolecularTestResult>) =
        results.firstNotNullOfOrNull { result -> result.msi }

    private fun configuredGenesAreNotEqual(gene1: String?, gene2: String?): Boolean {
        return (gene1 != null && gene2 != null && gene1 != gene2)
    }
}