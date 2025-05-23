package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.config.IhcTestConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.emc.questionnaire.Questionnaire
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory

class IhcTestsExtractor(
    private val molecularTestIhcCuration: CurationDatabase<IhcTestConfig>,
    private val molecularTestPdl1Curation: CurationDatabase<IhcTestConfig>,
) {

    fun extract(patientId: String, questionnaire: Questionnaire?): ExtractionResult<List<IhcTest>> {
        if (questionnaire == null) {
            return ExtractionResult(emptyList(), CurationExtractionEvaluation())
        }

        val curation = listOf(
            curate(patientId, CurationCategory.MOLECULAR_TEST_IHC, questionnaire.ihcTestResults, molecularTestIhcCuration),
            curate(patientId, CurationCategory.MOLECULAR_TEST_PDL1, questionnaire.pdl1TestResults, molecularTestPdl1Curation)
        )
            .flatten().fold(CurationResponse<IhcTestConfig>()) { acc, cur -> acc + cur }

        return ExtractionResult(curation.configs.filterNot(IhcTestConfig::ignore).map { it.curated!! }, curation.extractionEvaluation)
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) =
            IhcTestsExtractor(
                curationDatabaseContext.molecularTestIhcCuration,
                curationDatabaseContext.molecularTestPdl1Curation
            )

        private fun curate(
            patientId: String,
            curationCategory: CurationCategory,
            testResults: List<String>?,
            curationDatabase: CurationDatabase<IhcTestConfig>
        ) =
            testResults?.map {
                val input = CurationUtil.fullTrim(it)
                CurationResponse.createFromConfigs(
                    curationDatabase.find(input),
                    patientId,
                    curationCategory,
                    input,
                    curationCategory.categoryName
                )
            } ?: emptyList()
    }
}