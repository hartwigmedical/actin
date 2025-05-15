package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.SequencingTestConfig
import com.hartwig.actin.clinical.curation.config.SequencingTestResultConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.amplifications
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.fusions
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.deletions
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.msi
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.skippedExons
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.tmb
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.variants
import com.hartwig.actin.datamodel.clinical.SequencingTest
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.provided.ProvidedMolecularTest
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientRecord

class StandardSequencingTestExtractor(
    private val testCuration: CurationDatabase<SequencingTestConfig>,
    private val testResultCuration: CurationDatabase<SequencingTestResultConfig>
) :
    StandardDataExtractor<List<SequencingTest>> {

    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<SequencingTest>> {
        return ehrPatientRecord.molecularTests.orEmpty().map { test ->
            val testCurationResponse = CurationResponse.createFromConfigs(
                testCuration.find(test.test),
                ehrPatientRecord.patientDetails.hashedId,
                CurationCategory.SEQUENCING_TEST,
                test.test,
                "sequencing test",
                true
            )

            if (testCurationResponse.config()?.ignore == true) {
                ExtractionResult(emptyList(), testCurationResponse.extractionEvaluation)
            } else {
                extractTestResults(test, testCurationResponse, ehrPatientRecord.patientDetails.hashedId)
            }
        }
            .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, extractionResult ->
                ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
            }
    }

    private fun extractTestResults(
        test: ProvidedMolecularTest, sequencingTestCuration: CurationResponse<SequencingTestConfig>, patientId: String
    ): ExtractionResult<List<SequencingTest>> {
        return if (test.results.isNotEmpty() && test.results.all { it.ihcResult != null }) {
            ExtractionResult(emptyList(), CurationExtractionEvaluation())
        } else {
            val resultCurations = test.results.mapNotNull { result -> result.freeText?.let { curate(it, patientId) } }
            val allResults = resultCurations.flatMap {
                it.configs.filterNot(SequencingTestResultConfig::ignore).mapNotNull(SequencingTestResultConfig::curated)
            }.toSet()
            ExtractionResult(
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
                        )
                    }
                ),
                resultCurations.map(CurationResponse<SequencingTestResultConfig>::extractionEvaluation)
                    .fold(sequencingTestCuration.extractionEvaluation, CurationExtractionEvaluation::plus)
            )
        }
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