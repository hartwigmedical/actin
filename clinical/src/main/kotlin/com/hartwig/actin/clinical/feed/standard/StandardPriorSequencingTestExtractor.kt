package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.SequencingTestConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.PriorSequencingTest
import com.hartwig.actin.clinical.datamodel.SequencedAmplification
import com.hartwig.actin.clinical.datamodel.SequencedDeletedGene
import com.hartwig.actin.clinical.datamodel.SequencedFusion
import com.hartwig.actin.clinical.datamodel.SequencedSkippedExons
import com.hartwig.actin.clinical.datamodel.SequencedVariant

class StandardPriorSequencingTestExtractor(val curation: CurationDatabase<SequencingTestConfig>) :
    StandardDataExtractor<List<PriorSequencingTest>> {

    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<PriorSequencingTest>> {
        val extracted = ehrPatientRecord.molecularTests.map {
            val curatedResults =
                it.results.mapNotNull { result -> result.freeText }.map { text ->
                    CurationResponse.createFromConfigs(
                        curation.find(text),
                        ehrPatientRecord.patientDetails.hashedId,
                        CurationCategory.SEQUENCING_TEST,
                        text,
                        "sequencing test",
                        false
                    )
                }
            val allResults = it.results + curatedResults.flatMap { config -> config.configs }.mapNotNull { config -> config.curated }
            ExtractionResult(
                listOf(
                    PriorSequencingTest(
                        test = it.test,
                        date = it.date,
                        testedGenes = it.testedGenes,
                        variants = variants(allResults),
                        fusions = fusions(allResults),
                        amplifications = amplifications(allResults),
                        skippedExons = skippedExons(allResults),
                        deletedGenes = geneDeletions(allResults),
                        isMicrosatelliteUnstable = msi(allResults),
                        tumorMutationalBurden = tmb(allResults),
                    )
                ),
                curatedResults.map { curated -> curated.extractionEvaluation }
                    .fold(CurationExtractionEvaluation()) { acc, extraction -> acc + extraction }
            )
        }
        return extracted.fold(
            ExtractionResult(
                emptyList(),
                CurationExtractionEvaluation()
            )
        ) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }

    private fun geneDeletions(allResults: Set<ProvidedMolecularTestResult>) =
        allResults.filter { it.deletedGene != null }.map { SequencedDeletedGene(it.deletedGene!!) }.toSet()

    private fun tmb(results: Set<ProvidedMolecularTestResult>) =
        results.filter { result -> result.tmb != null }.firstNotNullOfOrNull { result -> result.tmb }

    private fun msi(results: Set<ProvidedMolecularTestResult>) =
        results.filter { result -> result.msi != null }.firstNotNullOfOrNull { result -> result.msi }

    private fun skippedExons(
        results: Set<ProvidedMolecularTestResult>
    ) = results.filter { result -> result.exonSkipStart != null }
        .map { result ->
            SequencedSkippedExons(
                result.gene!!,
                result.exonSkipStart!!,
                result.exonSkipEnd ?: result.exonSkipStart
            )
        }.toSet()

    private fun amplifications(results: Set<ProvidedMolecularTestResult>) =
        results.filter { result -> result.amplifiedGene != null }
            .map { result -> SequencedAmplification(result.amplifiedGene!!) }.toSet()

    private fun fusions(results: Set<ProvidedMolecularTestResult>) =
        results.filter { result -> result.fusionGeneUp != null || result.fusionGeneDown != null }
            .map { result -> SequencedFusion(result.fusionGeneUp, result.fusionGeneDown) }.toSet()

    private fun variants(results: Set<ProvidedMolecularTestResult>) =
        results.filter { result -> result.hgvsCodingImpact != null || result.hgvsProteinImpact != null }
            .map { result ->
                SequencedVariant(
                    result.gene
                        ?: throw IllegalArgumentException("Gene must be defined when hgvs protein/coding impact are indicated"),
                    result.hgvsCodingImpact,
                    result.hgvsProteinImpact,
                    result.transcript,
                    result.exon,
                    result.codon
                )
            }.toSet()
}