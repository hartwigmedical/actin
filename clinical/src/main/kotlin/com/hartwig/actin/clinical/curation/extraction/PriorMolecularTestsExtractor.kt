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

class PriorMolecularTestsExtractor(private val molecularTestCuration: CurationDatabase<MolecularTestConfig>) {

    fun extract(patientId: String, questionnaire: Questionnaire?): ExtractionResult<List<PriorMolecularTest>> {
        if (questionnaire == null) {
            return ExtractionResult(emptyList(), ExtractionEvaluation())
        }
        val curation = listOf(
            Pair("IHC", questionnaire.ihcTestResults ?: emptyList()),
            Pair("PD-L1", questionnaire.pdl1TestResults ?: emptyList())
        )
            .flatMap { (testType, testResults) ->
                testResults.map {
                    val input = CurationUtil.fullTrim(it)
                    CurationResponse.createFromConfigs(
                        molecularTestCuration.curate(input),
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
        fun create(curationDatabaseContext: CurationDatabaseContext) = PriorMolecularTestsExtractor(curationDatabaseContext.molecularTestCuration)
    }
}