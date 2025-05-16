package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.SequencingTestConfig
import com.hartwig.actin.clinical.curation.config.SequencingTestResultConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.emc.questionnaire.Questionnaire
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.amplifications
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.fusions
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.deletions
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.msi
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.skippedExons
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.tmb
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.variants
import com.hartwig.actin.datamodel.clinical.SequencingTest
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory

class SequencingTestExtractor(
    private val testCuration: CurationDatabase<SequencingTestConfig>,
    private val testResultCuration: CurationDatabase<SequencingTestResultConfig>
) {

    fun extract(patientId: String, questionnaire: Questionnaire?): ExtractionResult<List<SequencingTest>> {
        if (questionnaire == null || questionnaire.ihcTestResults.isNullOrEmpty()) {
            return ExtractionResult(emptyList(), CurationExtractionEvaluation())
        }

        val extracted = questionnaire.ihcTestResults.map { result ->
            val testCurationConfig =
                CurationResponse.createFromConfigs(
                    testCuration.find(result),
                    patientId,
                    CurationCategory.SEQUENCING_TEST,
                    result,
                    "sequencing test",
                    false
                )

            val curatedTestName = when (val config = testCurationConfig.config()) {
                null -> "Unknown test"
                else -> when {
                    config.ignore -> null
                    else -> config.curatedName
                }
            }

            curatedTestName?.let { name ->
                val sequencingResults = CurationResponse.createFromConfigs(
                    testResultCuration.find(result),
                    patientId,
                    CurationCategory.SEQUENCING_TEST_RESULT,
                    result,
                    "sequencing test result",
                    false
                )

                val notIgnoredResults = sequencingResults.configs.filter { !it.ignore }.toSet()

                if (notIgnoredResults.isNotEmpty()) {
                    ExtractionResult(
                        listOf(
                            SequencingTest(
                                test = name,
                                variants = variants(notIgnoredResults),
                                amplifications = amplifications(notIgnoredResults),
                                deletions = deletions(notIgnoredResults),
                                fusions = fusions(notIgnoredResults),
                                skippedExons = skippedExons(notIgnoredResults),
                                tumorMutationalBurden = tmb(notIgnoredResults),
                                isMicrosatelliteUnstable = msi(notIgnoredResults)
                            )
                        ),
                        sequencingResults.extractionEvaluation
                    )
                } else {
                    ExtractionResult(emptyList(), sequencingResults.extractionEvaluation.copy(warnings = emptySet()))
                }

            } ?: ExtractionResult(emptyList(), testCurationConfig.extractionEvaluation.copy(warnings = emptySet()))
        }
        return extracted.fold(
            ExtractionResult(emptyList(), CurationExtractionEvaluation())
        ) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }

    companion object {

        fun create(curationDatabaseContext: CurationDatabaseContext) =
            SequencingTestExtractor(
                curationDatabaseContext.sequencingTestCuration,
                curationDatabaseContext.sequencingTestResultCuration
            )
    }
}