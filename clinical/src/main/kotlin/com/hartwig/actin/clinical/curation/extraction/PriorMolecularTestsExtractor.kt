package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.clinical.feed.questionnaire.Questionnaire
import org.apache.logging.log4j.LogManager

class PriorMolecularTestsExtractor(
    private val molecularTestIhcCuration: CurationDatabase<MolecularTestConfig>,
    private val molecularTestPdl1Curation: CurationDatabase<MolecularTestConfig>,
) {

    private val logger = LogManager.getLogger(PriorMolecularTestsExtractor::class.java)

    fun extract(patientId: String, questionnaire: Questionnaire?): ExtractionResult<List<PriorMolecularTest>> {
        if (questionnaire == null) {
            return ExtractionResult(emptyList(), ExtractionEvaluation())
        }

        val curation = listOf(
            curate(patientId, CurationCategory.MOLECULAR_TEST_IHC, questionnaire.ihcTestResults, molecularTestIhcCuration),
            curate(patientId, CurationCategory.MOLECULAR_TEST_PDL1, questionnaire.pdl1TestResults, molecularTestPdl1Curation)
        )
            .flatten().fold(CurationResponse<MolecularTestConfig>()) { acc, cur -> acc + cur }

        return ExtractionResult(curation.configs.filterNot(MolecularTestConfig::ignore).map { it.curated!! }, curation.extractionEvaluation)
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) =
            PriorMolecularTestsExtractor(curationDatabaseContext.molecularTestIhcCuration, curationDatabaseContext.molecularTestPdl1Curation)

        private fun curate(patientId: String, curationCategory: CurationCategory, testResults: List<String>?, curationDatabase: CurationDatabase<MolecularTestConfig>) =
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