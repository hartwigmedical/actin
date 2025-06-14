package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.SequencingTestConfig
import com.hartwig.actin.clinical.curation.config.SequencingTestResultConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.amplifications
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.deletions
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.fusions
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.hrd
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.msi
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.negativeResults
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
) : StandardDataExtractor<List<SequencingTest>> {

    override fun extract(feedPatientRecord: FeedPatientRecord): ExtractionResult<List<SequencingTest>> {
        return feedPatientRecord.sequencingTests.map { test ->
            val testCurationResponse = CurationResponse.createFromConfigs(
                testCuration.find(test.name),
                feedPatientRecord.patientDetails.patientId,
                CurationCategory.SEQUENCING_TEST,
                test.name,
                "sequencing test",
                true
            )

            if (testCurationResponse.config()?.ignore == true) {
                ExtractionResult(emptyList(), testCurationResponse.extractionEvaluation)
            } else {
                extractTestResults(test, testCurationResponse, feedPatientRecord.patientDetails.patientId)
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
                        amplifications = amplifications(allResults),
                        deletions = deletions(allResults),
                        fusions = fusions(allResults),
                        skippedExons = skippedExons(allResults),
                        tumorMutationalBurden = tmb(allResults),
                        isMicrosatelliteUnstable = msi(allResults),
                        isHomologousRecombinationDeficient = hrd(allResults),
                        negativeResults = negativeResults(allResults),
                        knownSpecifications = test.knownSpecifications,
                        reportHash = test.reportHash
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