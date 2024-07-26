package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.PriorIHCTest

private const val IHC_STRING = "immunohistochemie"

class StandardPriorIHCTestExtractor(
    private val molecularTestCuration: CurationDatabase<MolecularTestConfig>
) : StandardDataExtractor<List<PriorIHCTest>> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<PriorIHCTest>> {
        val extractedIHCTests = extractIHC(molecularTestCuration, ehrPatientRecord)
        val extractedFromPriorOtherConditions = extractFromPriorOtherConditions(ehrPatientRecord)

        val curatedMolecularTestExtraction =
            (extractedIHCTests + extractedFromPriorOtherConditions).fold(
                ExtractionResult(
                    emptyList<PriorIHCTest>(), CurationExtractionEvaluation()
                )
            ) { acc, result ->
                ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
            }

        val extractedOtherMolecularTests = ehrPatientRecord.molecularTestHistory
            .flatMap { test -> test.results.map { test.date to it } }
            .filter { (_, result) -> result.ihcResult != null }
            .map { (date, result) ->
                PriorIHCTest(
                    item = result.gene,
                    measure = result.ihcResult,
                    measureDate = date,
                    impliesPotentialIndeterminateStatus = false,
                )
            }

        return ExtractionResult(
            curatedMolecularTestExtraction.extracted + extractedOtherMolecularTests,
            curatedMolecularTestExtraction.evaluation + CurationExtractionEvaluation()
        )
    }

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
        "molecular test",
        false
    )


    private fun extractIHC(
        molecularTestCuration: CurationDatabase<MolecularTestConfig>, ehrPatientRecord: ProvidedPatientRecord
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


