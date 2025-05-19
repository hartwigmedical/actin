package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.IhcTestConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.feed.datamodel.FeedPatientRecord
import java.time.LocalDate

class StandardIhcTestExtractor(
    private val molecularTestCuration: CurationDatabase<IhcTestConfig>
) : StandardDataExtractor<List<IhcTest>> {
    override fun extract(ehrPatientRecord: FeedPatientRecord): ExtractionResult<List<IhcTest>> {
        return ehrPatientRecord.ihcTests
            .asSequence()
            .mapNotNull { test ->
                val curations = listOf(test.name, "${ehrPatientRecord.patientDetails.patientId} | ${test.name}").map { curationString ->
                    curate(curationString, ehrPatientRecord.patientDetails.patientId, test.startDate)
                }
                if (curations.any { it.config()?.ignore == true }) {
                    null
                } else {
                    curations.firstOrNull { it.config() != null } ?: curations.firstOrNull { it.extractionEvaluation.warnings.isNotEmpty() }
                }
            }
            .map { ExtractionResult(it.configs.mapNotNull { config -> config.curated }, it.extractionEvaluation) }
            .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, result ->
                ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
            }
    }

    private fun curate(input: String, patientId: String, date: LocalDate?): CurationResponse<IhcTestConfig> {
        val configs = molecularTestCuration.find(input).map { config ->
            config.curated?.run { config.copy(curated = copy(measureDate = date)) } ?: config
        }.toSet()
        return CurationResponse.createFromConfigs(
            configs = configs,
            patientId = patientId,
            curationCategory = CurationCategory.MOLECULAR_TEST_IHC,
            inputText = input,
            configType = "molecular test ihc",
            requireUniqueness = false
        )
    }
}
