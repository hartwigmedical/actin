package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.IhcTestConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.feed.datamodel.FeedPatientRecord

private const val IHC_STRING = "immunohistochemie"

class StandardIhcTestExtractor(
    private val molecularTestCuration: CurationDatabase<IhcTestConfig>
) : StandardDataExtractor<List<IhcTest>> {
    override fun extract(ehrPatientRecord: FeedPatientRecord): ExtractionResult<List<IhcTest>> {
        val extractedIhcTestsFromTumorDifferentiation = extractIhc(molecularTestCuration, ehrPatientRecord)
        val extractedFromOtherConditions = extractFromOtherConditions(ehrPatientRecord)
        val extractedFromTumorDifferentiation = extractFromTumorDifferentiation(ehrPatientRecord)

        val curatedMolecularTestExtraction =
            (extractedIhcTestsFromTumorDifferentiation + extractedFromOtherConditions + extractedFromTumorDifferentiation).fold(
                ExtractionResult(
                    emptyList<IhcTest>(), CurationExtractionEvaluation()
                )
            ) { acc, result ->
                ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
            }

        val extractedIhcTestsFromIhcTestResults = extractFromMolecularTests(ehrPatientRecord)

        return ExtractionResult(
            curatedMolecularTestExtraction.extracted + extractedIhcTestsFromIhcTestResults.extracted,
            curatedMolecularTestExtraction.evaluation + extractedIhcTestsFromIhcTestResults.evaluation
        )
    }

    private fun extractFromMolecularTests(ehrPatientRecord: FeedPatientRecord) =
        ehrPatientRecord.ihcTests.asSequence()
            .mapNotNull {
                val curations = listOf(it.name, "${ehrPatientRecord.patientDetails.patientId} | $it").map { curationString ->
                    CurationResponse.createFromConfigs(
                        molecularTestCuration.find(curationString),
                        ehrPatientRecord.patientDetails.patientId,
                        CurationCategory.MOLECULAR_TEST_IHC,
                        curationString,
                        "molecular test",
                        false
                    )
                }
                if (curations.any { c -> c.config()?.ignore == true }) {
                    null
                } else {
                    curations.firstOrNull { c -> c.config() != null }
                        ?: curations.firstOrNull { c -> c.extractionEvaluation.warnings.isNotEmpty() }
                }
            }
            .map {
                ExtractionResult(
                    it.configs.mapNotNull { config -> config.curated },
                    it.extractionEvaluation
                )
            }.fold(
                ExtractionResult(
                    emptyList<IhcTest>(),
                    CurationExtractionEvaluation()
                )
            ) { acc, result ->
                ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
            }

    private fun extractFromTumorDifferentiation(ehrPatientRecord: FeedPatientRecord): List<ExtractionResult<List<IhcTest>>> =
        ehrPatientRecord.tumorDetails.tumorGradeDifferentiation?.split("\n")?.asSequence()
            ?.map { it.trim() }
            ?.filterNot { it.contains(IHC_STRING, ignoreCase = true) }
            ?.map { curateFromSecondarySource(it, ehrPatientRecord) }
            ?.filter {
                it.configs.isNotEmpty()
            }?.map {
                ExtractionResult(
                    it.configs.mapNotNull { config -> config.curated },
                    it.extractionEvaluation
                )
            }?.toList() ?: emptyList()

    private fun extractFromOtherConditions(ehrPatientRecord: FeedPatientRecord) =
        ehrPatientRecord.otherConditions.map {
            curateFromSecondarySource(it.name, ehrPatientRecord)
        }.filter {
            it.configs.isNotEmpty()
        }.map {
            ExtractionResult(
                it.configs.mapNotNull { config -> config.curated },
                it.extractionEvaluation
            )
        }

    private fun curateFromSecondarySource(
        input: String,
        ehrPatientRecord: FeedPatientRecord,
    ) = CurationResponse.createFromConfigs(
        molecularTestCuration.find(input),
        ehrPatientRecord.patientDetails.patientId,
        CurationCategory.MOLECULAR_TEST_IHC,
        input,
        "molecular test ihc",
        false
    )


    private fun extractIhc(
        molecularTestCuration: CurationDatabase<IhcTestConfig>, ehrPatientRecord: FeedPatientRecord
    ): List<ExtractionResult<List<IhcTest>>> {
        val linesWithIhc =
            ehrPatientRecord.tumorDetails.tumorGradeDifferentiation?.split("\n")
                ?.filter { it.contains(IHC_STRING, ignoreCase = true) }
                ?: emptyList()
        return linesWithIhc.map { it.replace("\n", "").replace("\r", "") }.map {
            val curationResponse = CurationResponse.createFromConfigs(
                molecularTestCuration.find(it),
                ehrPatientRecord.patientDetails.patientId,
                CurationCategory.MOLECULAR_TEST_IHC,
                it,
                "molecular test ihc",
                false
            )
            ExtractionResult(
                curationResponse.configs.mapNotNull { config -> config.curated },
                curationResponse.extractionEvaluation
            )
        }
    }
}


