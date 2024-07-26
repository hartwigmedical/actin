package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.PriorSequencingTest
import com.hartwig.actin.clinical.datamodel.SequencedAmplification
import com.hartwig.actin.clinical.datamodel.SequencedExonSkip
import com.hartwig.actin.clinical.datamodel.SequencedFusion
import com.hartwig.actin.clinical.datamodel.SequencedVariant

class StandardPriorSequencingTestExtractor : StandardDataExtractor<List<PriorSequencingTest>> {

    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<PriorSequencingTest>> {
        return ExtractionResult(ehrPatientRecord.molecularTestHistory.map {
            PriorSequencingTest(
                test = it.test,
                date = it.date,
                variants = variants(it),
                fusions = fusions(it),
                amplifications = amplifications(it),
                exonSkips = exonSkips(it)
            )
        }, CurationExtractionEvaluation())
    }

    private fun exonSkips(
        it: ProvidedMolecularTest
    ) = it.results.filter { result -> result.exonsSkipStart != null }
        .map { result ->
            SequencedExonSkip(
                result.gene!!,
                result.exonsSkipStart!!,
                result.exonsSkipEnd ?: result.exonsSkipStart
            )
        }.toSet()

    private fun amplifications(it: ProvidedMolecularTest) =
        it.results.filter { result -> result.amplifiedGene != null }
            .map { result -> SequencedAmplification(result.amplifiedGene!!, result.amplifiedChromosome) }.toSet()

    private fun fusions(it: ProvidedMolecularTest) =
        it.results.filter { result -> result.fusionGeneUp != null || result.fusionGeneDown != null }
            .map { result -> SequencedFusion(result.fusionGeneUp, result.fusionGeneDown) }.toSet()

    private fun variants(it: ProvidedMolecularTest) =
        it.results.filter { result -> result.hgvsCodingImpact != null || result.hgvsProteinImpact != null }
            .map { result ->
                SequencedVariant(
                    result.gene
                        ?: throw IllegalArgumentException("Gene must be defined when hgvs protein/coding impact are indicated"),
                    result.hgvsCodingImpact,
                    result.hgvsProteinImpact
                )
            }.toSet()
}