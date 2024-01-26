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
            Triple("IHC", questionnaire.ihcTestResults ?: emptyList(), molecularTestIhcCuration),
            Triple("PD-L1", questionnaire.pdl1TestResults ?: emptyList(), molecularTestPdl1Curation)
        )
            .flatMap { (testType, testResults, curation) ->
                testResults.map {
                    val input = CurationUtil.fullTrim(it)
                    CurationResponse.createFromConfigs(
                        curation.find(input),
                        patientId,
                        CurationCategory.MOLECULAR_TEST,
                        input,
                        "$testType molecular test"
                    )
                }
            }
            .fold(CurationResponse<MolecularTestConfig>()) { acc, cur -> acc + cur }

        return ExtractionResult(curation.configs.filterNot(MolecularTestConfig::ignore).map { it.curated!! }, curation.extractionEvaluation)
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) =
            PriorMolecularTestsExtractor(curationDatabaseContext.molecularTestIhcCuration, curationDatabaseContext.molecularTestPdl1Curation)
    }
}