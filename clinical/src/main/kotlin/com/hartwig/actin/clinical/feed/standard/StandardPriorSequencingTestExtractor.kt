package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.SequencingTestConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.PriorSequencingTest
import com.hartwig.actin.datamodel.clinical.SequencedAmplification
import com.hartwig.actin.datamodel.clinical.SequencedDeletedGene
import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.clinical.SequencedSkippedExons
import com.hartwig.actin.datamodel.clinical.SequencedVariant
import kotlin.reflect.full.memberProperties

class StandardPriorSequencingTestExtractor(val curation: CurationDatabase<SequencingTestConfig>) :
    StandardDataExtractor<List<PriorSequencingTest>> {

    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<PriorSequencingTest>> {
        val extracted = ehrPatientRecord.molecularTests.map { test ->
            val (onlyFreeTextResults, populatedResults) = test.results.partition { checkAllFieldsNull(it) }
            val mandatoryCurationTestResults = curate(ehrPatientRecord, onlyFreeTextResults)
            val allResults = test.results + extract(mandatoryCurationTestResults) + extract(curate(ehrPatientRecord, populatedResults))
            ExtractionResult(
                listOf(
                    PriorSequencingTest(
                        test = test.test,
                        date = test.date,
                        testedGenes = test.testedGenes,
                        variants = variants(allResults),
                        fusions = fusions(allResults),
                        amplifications = amplifications(allResults),
                        skippedExons = skippedExons(allResults),
                        deletedGenes = geneDeletions(allResults),
                        isMicrosatelliteUnstable = msi(allResults),
                        tumorMutationalBurden = tmb(allResults),
                    )
                ),
                mandatoryCurationTestResults.map { curated -> curated.extractionEvaluation }
                    .fold(CurationExtractionEvaluation()) { acc, extraction -> acc + extraction }
            )
        }
        return extracted.fold(
            ExtractionResult(emptyList(), CurationExtractionEvaluation())
        ) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }

    private fun extract(curationResults: List<CurationResponse<SequencingTestConfig>>) =
        curationResults.flatMap { it.configs }.mapNotNull { it.curated }

    private fun curate(
        ehrPatientRecord: ProvidedPatientRecord,
        results: Collection<ProvidedMolecularTestResult>
    ) = results.mapNotNull { result -> result.freeText }
        .map { text ->
            CurationResponse.createFromConfigs(
                curation.find(text),
                ehrPatientRecord.patientDetails.hashedId,
                CurationCategory.SEQUENCING_TEST,
                text,
                "sequencing test",
                false
            )
        }

    private fun checkAllFieldsNull(result: ProvidedMolecularTestResult) =
        ProvidedMolecularTestResult::class.memberProperties.filter { it.name != "freeText" }.all { it.get(result) == null }

    private fun geneDeletions(allResults: Set<ProvidedMolecularTestResult>) =
        allResults.mapNotNull { it.deletedGene?.let { gene -> SequencedDeletedGene(gene) } }.toSet()

    private fun tmb(results: Set<ProvidedMolecularTestResult>) =
        results.firstNotNullOfOrNull { result -> result.tmb }

    private fun msi(results: Set<ProvidedMolecularTestResult>) =
        results.firstNotNullOfOrNull { result -> result.msi }

    private fun skippedExons(
        results: Set<ProvidedMolecularTestResult>
    ) = results.filter { result -> result.exonSkipStart != null }
        .map { result ->
            SequencedSkippedExons(
                result.gene!!,
                result.exonSkipStart!!,
                result.exonSkipEnd ?: result.exonSkipStart,
                result.transcript
            )
        }.toSet()

    private fun amplifications(results: Set<ProvidedMolecularTestResult>) =
        results.mapNotNull { result -> result.amplifiedGene?.let { SequencedAmplification(it) } }.toSet()

    private fun fusions(results: Set<ProvidedMolecularTestResult>) =
        results.filter { result -> result.fusionGeneUp != null || result.fusionGeneDown != null }
            .map { result -> SequencedFusion(result.fusionGeneUp, result.fusionGeneDown) }.toSet()

    private fun variants(results: Set<ProvidedMolecularTestResult>) =
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