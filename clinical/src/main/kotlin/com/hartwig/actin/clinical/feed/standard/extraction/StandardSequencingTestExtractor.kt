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

class StandardSequencingTestExtractor(
    private val testCuration: CurationDatabase<SequencingTestConfig>,
    private val testResultCuration: CurationDatabase<SequencingTestResultConfig>
) :
    StandardDataExtractor<List<SequencingTest>> {

    override fun extract(ehrPatientRecord: FeedPatientRecord): ExtractionResult<List<SequencingTest>> {
        val extracted = ehrPatientRecord.sequencingTests.mapNotNull { test ->
            val testCurationConfig = CurationResponse.createFromConfigs(
                testCuration.find(test.name),
                ehrPatientRecord.patientDetails.patientId,
                CurationCategory.SEQUENCING_TEST,
                test.name,
                "sequencing test",
                false
            )

            testCurationConfig.config()?.takeUnless { it.ignore }?.let { testCuration ->
                    val onlyFreeTextResults = test.results.toSet()
                    val allResults = this.curate(onlyFreeTextResults, ehrPatientRecord.patientDetails.patientId)
                        .flatMap { it.configs }
                        .filterNot { it.ignore }
                        .toSet()
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
                            this.curate(onlyFreeTextResults, ehrPatientRecord.patientDetails.patientId).map { curated -> curated.extractionEvaluation }
                                .fold(CurationExtractionEvaluation()) { acc, extraction -> acc + extraction }
                        )
                    } else null
            } ?: ExtractionResult(emptyList(), testCurationConfig.extractionEvaluation)
        }
        return extracted.fold(
            ExtractionResult(emptyList(), CurationExtractionEvaluation())
        ) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }

    private fun curate(results: Collection<String>, patientId: String) = results.map { text ->
            CurationResponse.createFromConfigs(
                testResultCuration.find(text),
                patientId,
                CurationCategory.SEQUENCING_TEST_RESULT,
                text,
                "sequencing test result",
                false
            )
        }
}