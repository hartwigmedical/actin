package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.datamodel.clinical.SequencedAmplification
import com.hartwig.actin.datamodel.clinical.SequencedDeletedGene
import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.clinical.SequencedSkippedExons
import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.clinical.provided.ProvidedMolecularTestResult

object StandardSequencingTestExtractorFunctions {

    fun geneDeletions(allResults: Set<ProvidedMolecularTestResult>) =
        allResults.mapNotNull { it.deletedGene?.let { gene -> SequencedDeletedGene(gene, it.transcript) } }.toSet()

    fun tmb(results: Set<ProvidedMolecularTestResult>) =
        results.firstNotNullOfOrNull { result -> result.tmb }

    fun msi(results: Set<ProvidedMolecularTestResult>) =
        results.firstNotNullOfOrNull { result -> result.msi }

    fun skippedExons(
        results: Set<ProvidedMolecularTestResult>
    ) = results.mapNotNull { result ->
        result.exonSkipStart?.let { exonSkipStart ->
            SequencedSkippedExons(
                result.gene!!,
                result.exonSkipStart!!,
                result.exonSkipEnd ?: exonSkipStart,
                result.transcript
            )
        }
    }.toSet()

    fun amplifications(results: Set<ProvidedMolecularTestResult>) =
        results.mapNotNull { it.amplifiedGene?.let { gene -> SequencedAmplification(gene, it.transcript) } }.toSet()

    fun fusions(results: Set<ProvidedMolecularTestResult>) =
        results.filter { result -> result.fusionGeneUp != null || result.fusionGeneDown != null }
            .map { result ->
                SequencedFusion(
                    result.fusionGeneUp,
                    result.fusionGeneDown,
                    result.fusionTranscriptUp,
                    result.fusionTranscriptDown,
                    result.fusionExonUp,
                    result.fusionExonDown
                )
            }.toSet()

    fun variants(results: Set<ProvidedMolecularTestResult>) =
        results.filter { result -> result.hgvsCodingImpact != null || result.hgvsProteinImpact != null }
            .map { result ->
                SequencedVariant(
                    result.vaf,
                    result.gene
                        ?: throw IllegalArgumentException("Gene must be defined when hgvs protein/coding impact are indicated"),
                    result.hgvsCodingImpact,
                    result.hgvsProteinImpact,
                    result.transcript,
                    result.codon,
                    result.exon
                )
            }.toSet()
}