package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.SequencingTestConfig
import com.hartwig.actin.clinical.curation.config.SequencingTestResultConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.amplifications
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.fusions
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.geneDeletions
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.msi
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.skippedExons
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.tmb
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.variants
import com.hartwig.actin.datamodel.clinical.SequencingTest
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.provided.ProvidedMolecularTestResult
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientRecord
import kotlin.reflect.full.memberProperties

class StandardSequencingTestExtractor(
    private val testCuration: CurationDatabase<SequencingTestConfig>,
    private val testResultCuration: CurationDatabase<SequencingTestResultConfig>
) :
    StandardDataExtractor<List<SequencingTest>> {

    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<SequencingTest>> {
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
                    val nonIhcTestResults = test.results.filter { it.ihcResult == null }.toSet()
                    val (onlyFreeTextResults, populatedResults) = nonIhcTestResults
                        .partition { checkAllFieldsNull(it) }
                    val mandatoryCurationTestResults = curate(ehrPatientRecord, onlyFreeTextResults)
                    val optionalCurationTestResults = curate(ehrPatientRecord, populatedResults)
                    val allResults =
                        removeCurated(removeCurated(nonIhcTestResults, mandatoryCurationTestResults), optionalCurationTestResults) +
                                extract(mandatoryCurationTestResults + optionalCurationTestResults)
                    if (allResults.isNotEmpty()) {
                        ExtractionResult(
                            listOf(
                                SequencingTest(
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

}