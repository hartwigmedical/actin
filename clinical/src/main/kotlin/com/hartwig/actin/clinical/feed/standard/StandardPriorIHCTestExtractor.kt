package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.IHCTestConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.PriorIHCTest

private const val IHC_STRING = "immunohistochemie"

class StandardPriorIHCTestExtractor(
    private val molecularTestCuration: CurationDatabase<IHCTestConfig>
) : StandardDataExtractor<List<PriorIHCTest>> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<PriorIHCTest>> {
        val extractedIHCTestsFromTumorDifferentiation = extractIHC(molecularTestCuration, ehrPatientRecord)
        val extractedFromPriorOtherConditions = extractFromPriorOtherConditions(ehrPatientRecord)
        val extractedFromTumorDifferentiation = extractFromTumorDifferentiation(ehrPatientRecord)

        val curatedMolecularTestExtraction =
            (extractedIHCTestsFromTumorDifferentiation + extractedFromPriorOtherConditions + extractedFromTumorDifferentiation).fold(
                ExtractionResult(
                    emptyList<PriorIHCTest>(), CurationExtractionEvaluation()
                )
            ) { acc, result ->
                ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
            }

        val extractedOtherMolecularTestsLegacy = ehrPatientRecord.molecularTestHistory.map {
            PriorIHCTest(
                test = it.type,
                item = it.measure,
                measure = it.result,
                measureDate = it.resultDate,
                impliesPotentialIndeterminateStatus = false,
                scoreText = it.resultType
            )
        }

        val extractedIHCTests = ehrPatientRecord.molecularTests.asSequence()
            .flatMap { it.results }
            .mapNotNull { it.ihcResult }
            .map {
                CurationResponse.createFromConfigs(
                    molecularTestCuration.find(it),
                    ehrPatientRecord.patientDetails.hashedId,
                    CurationCategory.MOLECULAR_TEST_IHC,
                    it,
                    "molecular test",
                    false
                )
            }
            .map { ExtractionResult(it.configs.mapNotNull { config -> config.curated }, it.extractionEvaluation) }
            .fold(ExtractionResult(emptyList<PriorIHCTest>(), CurationExtractionEvaluation())) { acc, result ->
                ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
            }

        return ExtractionResult(
            curatedMolecularTestExtraction.extracted + extractedOtherMolecularTestsLegacy + extractedIHCTests.extracted,
            curatedMolecularTestExtraction.evaluation + extractedIHCTests.evaluation
        )
    }

    private fun extractFromTumorDifferentiation(ehrPatientRecord: ProvidedPatientRecord): List<ExtractionResult<List<PriorIHCTest>>> =
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

    private fun extractFromPriorOtherConditions(ehrPatientRecord: ProvidedPatientRecord) = ehrPatientRecord.priorOtherConditions.map {
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
        ehrPatientRecord: ProvidedPatientRecord,
    ) = CurationResponse.createFromConfigs(
        molecularTestCuration.find(input),
        ehrPatientRecord.patientDetails.hashedId,
        CurationCategory.MOLECULAR_TEST_IHC,
        input,
        "molecular test ihc",
        false
    )


    private fun extractIHC(
        molecularTestCuration: CurationDatabase<IHCTestConfig>, ehrPatientRecord: ProvidedPatientRecord
    ): List<ExtractionResult<List<PriorIHCTest>>> {
        val linesWithIHC =
            ehrPatientRecord.tumorDetails.tumorGradeDifferentiation?.split("\n")?.filter { it.contains(IHC_STRING, ignoreCase = true) }
                ?: emptyList()
        return linesWithIHC.map { it.replace("\n", "").replace("\r", "") }.map {
            val curationResponse = CurationResponse.createFromConfigs(
                molecularTestCuration.find(it),
                ehrPatientRecord.patientDetails.hashedId,
                CurationCategory.MOLECULAR_TEST_IHC,
                it,
                "molecular test ihc",
                false
            )
            ExtractionResult(curationResponse.configs.mapNotNull { config -> config.curated }, curationResponse.extractionEvaluation)
        }
    }
}


