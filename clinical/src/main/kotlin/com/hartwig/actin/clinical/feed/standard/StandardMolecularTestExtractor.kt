package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest

private const val IHC_STRING = "immunohistochemie"

class StandardMolecularTestExtractor(
    private val molecularTestCuration: CurationDatabase<MolecularTestConfig>
) : StandardDataExtractor<List<PriorMolecularTest>> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<PriorMolecularTest>> {
        val extractedIHCTests = extractIHC(molecularTestCuration, ehrPatientRecord)
        val extractedFromPriorOtherConditions = extractFromPriorOtherConditions(ehrPatientRecord)
        val extractedFromTumorDifferentiation = extractFromTumorDifferentiation(ehrPatientRecord)

        val curatedMolecularTestExtraction =
            (extractedIHCTests + extractedFromPriorOtherConditions + extractedFromTumorDifferentiation).fold(
                ExtractionResult(
                    emptyList<PriorMolecularTest>(), CurationExtractionEvaluation()
                )
            ) { acc, result ->
                ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
            }

        val extractedOtherMolecularTests = ehrPatientRecord.molecularTestHistory.map {
            PriorMolecularTest(
                test = it.type,
                item = it.measure,
                measure = it.result,
                measureDate = it.resultDate,
                impliesPotentialIndeterminateStatus = false,
                scoreText = it.resultType
            )
        }

        return ExtractionResult(
            curatedMolecularTestExtraction.extracted + extractedOtherMolecularTests,
            curatedMolecularTestExtraction.evaluation + CurationExtractionEvaluation()
        )
    }

    private fun extractFromTumorDifferentiation(ehrPatientRecord: ProvidedPatientRecord): List<ExtractionResult<List<PriorMolecularTest>>> =
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
        "molecular test",
        false
    )


    private fun extractIHC(
        molecularTestCuration: CurationDatabase<MolecularTestConfig>, ehrPatientRecord: ProvidedPatientRecord
    ): List<ExtractionResult<List<PriorMolecularTest>>> {
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


