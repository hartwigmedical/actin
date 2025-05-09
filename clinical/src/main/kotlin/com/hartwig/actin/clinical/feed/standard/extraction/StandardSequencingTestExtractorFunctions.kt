package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.config.SequencingTestResultConfig
import com.hartwig.actin.datamodel.clinical.SequencedAmplification
import com.hartwig.actin.datamodel.clinical.SequencedDeletedGene
import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.clinical.SequencedSkippedExons
import com.hartwig.actin.datamodel.clinical.SequencedVariant

object StandardSequencingTestExtractorFunctions {

    fun geneDeletions(allResults: Set<SequencingTestResultConfig>) =
        allResults.mapNotNull { it.deletedGene?.let { gene -> SequencedDeletedGene(gene, it.transcript) } }.toSet()

    fun tmb(results: Set<SequencingTestResultConfig>) = results.firstNotNullOfOrNull { result -> result.tmb }

    fun msi(results: Set<SequencingTestResultConfig>) = results.firstNotNullOfOrNull { result -> result.msi }

    fun skippedExons(results: Set<SequencingTestResultConfig>) = results.mapNotNull { result ->
        result.exonSkipStart?.let { exonSkipStart ->
            SequencedSkippedExons(
                result.gene!!,
                exonSkipStart,
                result.exonSkipEnd ?: exonSkipStart,
                result.transcript
            )
        }
    }.toSet()

    fun amplifications(results: Set<SequencingTestResultConfig>) =
        results.mapNotNull { it.amplifiedGene?.let { gene -> SequencedAmplification(gene, it.transcript) } }.toSet()

    fun fusions(results: Set<SequencingTestResultConfig>) =
        results.filter { result -> result.fusionGeneUp != null || result.fusionGeneDown != null }
            .map { result -> SequencedFusion(result.fusionGeneUp, result.fusionGeneDown) }
            .toSet()

    fun variants(results: Set<SequencingTestResultConfig>) =
        results.filter { result -> result.hgvsCodingImpact != null || result.hgvsProteinImpact != null }
            .map { result ->
                SequencedVariant(
                    gene = result.gene
                        ?: throw IllegalArgumentException("Gene must be defined when hgvs protein/coding impact are indicated"),
                    hgvsCodingImpact = result.hgvsCodingImpact,
                    hgvsProteinImpact = result.hgvsProteinImpact,
                    transcript = result.transcript,
                    exon = result.codon,
                    codon = result.exon
                )
            }.toSet()
}