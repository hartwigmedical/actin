package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.SequencingTestConfig
import com.hartwig.actin.clinical.curation.config.SequencingTestResultConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.PriorSequencingTest
import com.hartwig.actin.datamodel.clinical.SequencedAmplification
import com.hartwig.actin.datamodel.clinical.SequencedDeletedGene
import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.clinical.SequencedSkippedExons
import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.provided.ProvidedMolecularTestResult
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientRecord
import kotlin.reflect.full.memberProperties

class StandardPriorSequencingTestExtractor(
    private val testCuration: CurationDatabase<SequencingTestConfig>,
    private val testResultCuration: CurationDatabase<SequencingTestResultConfig>
) :
    StandardDataExtractor<List<PriorSequencingTest>> {

    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<PriorSequencingTest>> {
        val extracted = ehrPatientRecord.molecularTests.mapNotNull { test ->
            val testCurationConfig =
                CurationResponse.createFromConfigs(
                    testCuration.find(test.test),
                    ehrPatientRecord.patientDetails.hashedId,
                    CurationCategory.SEQUENCING_TEST,
                    test.test,
                    "sequencing test",
                    false
                )
            testCurationConfig.config()?.let { testCuration ->
                if (!testCuration.ignore) {
                    val nonIHCTestResults = test.results.filter { it.ihcResult == null }.toSet()
                    val (onlyFreeTextResults, populatedResults) = nonIHCTestResults
                        .partition { checkAllFieldsNull(it) }
                    val mandatoryCurationTestResults = curate(ehrPatientRecord, onlyFreeTextResults)
                    val optionalCurationTestResults = curate(ehrPatientRecord, populatedResults)
                    val allResults =
                        removeCurated(removeCurated(nonIHCTestResults, mandatoryCurationTestResults), optionalCurationTestResults) +
                                extract(mandatoryCurationTestResults + optionalCurationTestResults)
                    if (allResults.isNotEmpty()) {
                        ExtractionResult(
                            listOf(
                                PriorSequencingTest(
                                    test = testCuration.curatedName,
                                    date = test.date,
                                    variants = variants(allResults),
                                    fusions = fusions(allResults),
                                    amplifications = amplifications(allResults),
                                    skippedExons = skippedExons(allResults),
                                    deletedGenes = geneDeletions(allResults),
                                    isMicrosatelliteUnstable = msi(allResults),
                                    tumorMutationalBurden = tmb(allResults)
                                )
                            ),
                            mandatoryCurationTestResults.map { curated -> curated.extractionEvaluation }
                                .fold(CurationExtractionEvaluation()) { acc, extraction -> acc + extraction }
                        )
                    } else null
                } else {
                    null
                }
            } ?: ExtractionResult(emptyList(), testCurationConfig.extractionEvaluation)
        }
        return extracted.fold(
            ExtractionResult(emptyList(), CurationExtractionEvaluation())
        ) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }

    private fun removeCurated(
        original: Set<ProvidedMolecularTestResult>,
        curated: List<CurationResponse<SequencingTestResultConfig>>
    ): Set<ProvidedMolecularTestResult> {
        val freeTexts = curated.flatMap { it.configs }.map { it.input }.toSet()
        return original.filter { it.freeText !in freeTexts }.toSet()
    }

    private fun extract(curationResults: List<CurationResponse<SequencingTestResultConfig>>) =
        curationResults.flatMap { it.configs }.filter { !it.ignore }.mapNotNull { it.curated }

    private fun curate(
        ehrPatientRecord: ProvidedPatientRecord,
        results: Collection<ProvidedMolecularTestResult>
    ) = results.mapNotNull { result -> result.freeText }
        .map { text ->
            CurationResponse.createFromConfigs(
                testResultCuration.find(text),
                ehrPatientRecord.patientDetails.hashedId,
                CurationCategory.SEQUENCING_TEST_RESULT,
                text,
                "sequencing test result",
                false
            )
        }

    private fun checkAllFieldsNull(result: ProvidedMolecularTestResult) =
        ProvidedMolecularTestResult::class.memberProperties.filter { it.name != "freeText" }.all { it.get(result) == null }

    private fun geneDeletions(allResults: Set<ProvidedMolecularTestResult>) =
        allResults.mapNotNull { it.deletedGene?.let { gene -> SequencedDeletedGene(gene, it.transcript) } }.toSet()

    private fun tmb(results: Set<ProvidedMolecularTestResult>) =
        results.firstNotNullOfOrNull { result -> result.tmb }

    private fun msi(results: Set<ProvidedMolecularTestResult>) =
        results.firstNotNullOfOrNull { result -> result.msi }

    private fun skippedExons(
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

    private fun amplifications(results: Set<ProvidedMolecularTestResult>) =
        results.mapNotNull { it.amplifiedGene?.let { gene -> SequencedAmplification(gene, it.transcript) } }.toSet()

    private fun fusions(results: Set<ProvidedMolecularTestResult>) =
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