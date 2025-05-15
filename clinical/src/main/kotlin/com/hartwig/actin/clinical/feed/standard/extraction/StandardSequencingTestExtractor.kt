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
import com.hartwig.feed.datamodel.FeedPatientRecord
import com.hartwig.feed.datamodel.FeedSequencingTest

class StandardSequencingTestExtractor(
    private val testCuration: CurationDatabase<SequencingTestConfig>,
    private val testResultCuration: CurationDatabase<SequencingTestResultConfig>
) :
    StandardDataExtractor<List<SequencingTest>> {

    override fun extract(ehrPatientRecord: FeedPatientRecord): ExtractionResult<List<SequencingTest>> {
        return ehrPatientRecord.sequencingTests.map { test ->
            val testCurationResponse = CurationResponse.createFromConfigs(
                testCuration.find(test.name),
                ehrPatientRecord.patientDetails.patientId,
                CurationCategory.SEQUENCING_TEST,
                test.name,
                "sequencing test",
                true
            )

            if (testCurationResponse.config()?.ignore == true) {
                ExtractionResult(emptyList(), testCurationResponse.extractionEvaluation)
            } else {
                extractTestResults(test, testCurationResponse, ehrPatientRecord.patientDetails.patientId)
            }
        }
            .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, extractionResult ->
                ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
            }
    }

    private fun extractTestResults(
        test: FeedSequencingTest, sequencingTestCuration: CurationResponse<SequencingTestConfig>, patientId: String
    ): ExtractionResult<List<SequencingTest>> {
        val resultCurations = test.results.map { result -> curate(result, patientId) }
        val allResults = resultCurations.flatMap { it.configs.filterNot(SequencingTestResultConfig::ignore) }.toSet()
        return ExtractionResult(
            listOfNotNull(
                sequencingTestCuration.config()?.let {
                    SequencingTest(
                        test = it.curatedName,
                        date = test.date,
                        variants = variants(allResults),
                        fusions = fusions(allResults),
                        amplifications = amplifications(allResults),
                        skippedExons = skippedExons(allResults),
                        deletedGenes = geneDeletions(allResults),
                        isMicrosatelliteUnstable = msi(allResults),
                        tumorMutationalBurden = tmb(allResults)
                    )
                }
            ),
            resultCurations.map(CurationResponse<SequencingTestResultConfig>::extractionEvaluation)
                .fold(sequencingTestCuration.extractionEvaluation, CurationExtractionEvaluation::plus)
        )
    }

    private fun curate(result: String, patientId: String) = CurationResponse.createFromConfigs(
        testResultCuration.find(result),
        patientId,
        CurationCategory.SEQUENCING_TEST_RESULT,
        result,
        "sequencing test result",
        false
    )
}