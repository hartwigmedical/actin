package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest

private const val IHC_STRING = "immunohistochemie"

class EhrMolecularTestExtractor(
    private val molecularTestCuration: CurationDatabase<MolecularTestConfig>,
) : EhrExtractor<List<PriorMolecularTest>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<PriorMolecularTest>> {
        val extractedIHCTests = extractIHC(molecularTestCuration, ehrPatientRecord)

        val extractedOtherMolecularTests = ehrPatientRecord.molecularTestHistory.map {
            PriorMolecularTest(
                test = it.type,
                item = it.measure,
                measure = it.result,
                measureDate = it.resultDate,
                impliesPotentialIndeterminateStatus = false
            )
        }

        return ExtractionResult(extractedIHCTests.extracted + extractedOtherMolecularTests,
            extractedIHCTests.evaluation + CurationExtractionEvaluation())
    }
}

fun extractIHC(molecularTestCuration: CurationDatabase<MolecularTestConfig>, ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<PriorMolecularTest>> {
    val linesWithIHC = ehrPatientRecord.tumorDetails.tumorGradeDifferentiation
        .split("\n")
        .filter { it.contains(IHC_STRING, ignoreCase = true) }
    return linesWithIHC
        .map { it.replace("\n", "").replace("\r", "") }
        .map {
            val curationResponse = CurationResponse.createFromConfigs(
                molecularTestCuration.find(it),
                ehrPatientRecord.patientDetails.hashedId,
                CurationCategory.MOLECULAR_TEST_IHC,
                it,
                "molecular test ihc"
            )
            ExtractionResult(listOfNotNull(curationResponse.config()?.curated), curationResponse.extractionEvaluation)
        }.fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, result ->
            ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
        }
}
