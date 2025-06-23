package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
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
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractorFunctions.viruses
import com.hartwig.actin.datamodel.clinical.SequencingTest
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.feed.datamodel.FeedIhcResult

class SequencingTestExtractor(
    private val testCuration: CurationDatabase<SequencingTestConfig>,
    private val testResultCuration: CurationDatabase<SequencingTestResultConfig>
) {

    fun extract(patientId: String, entries: List<FeedIhcResult>): ExtractionResult<List<SequencingTest>> {
        if (entries.isEmpty()) {
            return ExtractionResult(emptyList(), CurationExtractionEvaluation())
        }

        val extracted = entries.map { result ->
            val testCurationConfig =
                CurationResponse.createFromConfigs(
                    testCuration.find(result.name),
                    patientId,
                    CurationCategory.SEQUENCING_TEST,
                    result.name,
                    "sequencing test",
                    true
                )

            val sequencingResults = CurationResponse.createFromConfigs(
                testResultCuration.find(result.name),
                patientId,
                CurationCategory.SEQUENCING_TEST_RESULT,
                result.name,
                "sequencing test result",
                false
            )

            if (testCurationConfig.configs.isEmpty() && sequencingResults.configs.isEmpty()) {
                ExtractionResult(emptyList<List<SequencingTest>>(), CurationExtractionEvaluation())
            }

            val sequencingTestConfig = testCurationConfig.config()?.takeUnless { it.ignore }
            val sequencingTestResultConfig = sequencingResults.configs.filterNot { it.ignore }.toSet()

            val extractionEvaluation = sequencingResults.extractionEvaluation + testCurationConfig.extractionEvaluation
            when {
                sequencingTestConfig == null && sequencingTestResultConfig.isEmpty() ->
                    ExtractionResult(emptyList(), extractionEvaluation.copy(warnings = emptySet()))

                sequencingTestConfig != null && sequencingTestResultConfig.isNotEmpty() ->
                    ExtractionResult(
                        listOf(
                            SequencingTest(
                                test = testCurationConfig.config()?.curatedName ?: "",
                                variants = variants(sequencingTestResultConfig),
                                amplifications = amplifications(sequencingTestResultConfig),
                                deletions = deletions(sequencingTestResultConfig),
                                fusions = fusions(sequencingTestResultConfig),
                                skippedExons = skippedExons(sequencingTestResultConfig),
                                viruses = viruses(sequencingTestResultConfig),
                                tumorMutationalBurden = tmb(sequencingTestResultConfig),
                                isMicrosatelliteUnstable = msi(sequencingTestResultConfig),
                                isHomologousRecombinationDeficient = hrd(sequencingTestResultConfig),
                                negativeResults = negativeResults(sequencingTestResultConfig),
                                knownSpecifications = sequencingTestConfig.allowFromAnyLab == true
                            )
                        ),
                        extractionEvaluation
                    )

                else ->
                    ExtractionResult(emptyList(), extractionEvaluation)
            }
        }
        return extracted.fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, res ->
            ExtractionResult(acc.extracted + res.extracted, acc.evaluation + res.evaluation)
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