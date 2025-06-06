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
    override fun extract(feedPatientRecord: FeedPatientRecord): ExtractionResult<List<IhcTest>> {
        return feedPatientRecord.ihcTests
            .asSequence()
            .mapNotNull { test ->
                listOf(test.name, "${feedPatientRecord.patientDetails.patientId} | ${test.name}")
                    .map { curationString -> curate(curationString, feedPatientRecord.patientDetails.patientId, test.startDate) }
                    .takeUnless { it.any { curation -> curation.config()?.ignore == true } }
                    ?.let { curations ->
                        curations.firstOrNull { it.config() != null }
                            ?: curations.firstOrNull { it.extractionEvaluation.warnings.isNotEmpty() }
                    }
                    ?.let { curation ->
                        ExtractionResult(
                            curation.configs.mapNotNull { config -> config.curated?.copy(reportHash = test.reportHash) },
                            curation.extractionEvaluation
                        )
                    }
            }
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
