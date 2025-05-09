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
        return ehrPatientRecord.sequencingTests.mapNotNull { test ->
            val testCurationConfig = CurationResponse.createFromConfigs(
                testCuration.find(test.name),
                ehrPatientRecord.patientDetails.patientId,
                CurationCategory.SEQUENCING_TEST,
                test.name,
                "sequencing test",
                false
            )

            testCurationConfig.config()?.takeUnless { it.ignore }
                ?.let { testCuration -> extractTestResults(test, testCuration, ehrPatientRecord.patientDetails.patientId) }
                ?: ExtractionResult(emptyList(), testCurationConfig.extractionEvaluation)
        }
            .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, extractionResult ->
                ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
            }
    }

    private fun extractTestResults(
        test: FeedSequencingTest, sequencingTestCuration: SequencingTestConfig, patientId: String
    ): ExtractionResult<List<SequencingTest>>? = test.results.takeIf { it.isNotEmpty() }
        ?.map { result -> curate(result, patientId) }
        ?.let { resultCurations ->
            val allResults = resultCurations.flatMap { it.configs.filterNot(SequencingTestResultConfig::ignore) }.toSet()
            ExtractionResult(
                listOf(
                    SequencingTest(
                        test = sequencingTestCuration.curatedName,
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
                resultCurations.map(CurationResponse<SequencingTestResultConfig>::extractionEvaluation)
                    .reduce(CurationExtractionEvaluation::plus)
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