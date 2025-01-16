package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.emc.questionnaire.Questionnaire
import com.hartwig.actin.datamodel.clinical.Comorbidity

class ComorbidityExtractor(private val comorbidityCuration: CurationDatabase<ComorbidityConfig>) {

    fun extract(patientId: String, questionnaire: Questionnaire?): ExtractionResult<List<Comorbidity>?> {
        return if (questionnaire == null) {
            ExtractionResult(null, CurationExtractionEvaluation())
        } else {
            val curation = listOfNotNull(
                questionnaire.nonOncologicalHistory?.let {
                    extractCategory(patientId, it, CurationCategory.NON_ONCOLOGICAL_HISTORY, "non-oncological history")
                },
                questionnaire.complications?.let {
                    extractCategory(patientId, it, CurationCategory.COMPLICATION, "complication")
                }
            ).flatten()
                .fold(CurationResponse<ComorbidityConfig>()) { acc, cur -> acc + cur }

            val curated = curation.configs.filterNot(ComorbidityConfig::ignore).mapNotNull(ComorbidityConfig::curated)

            ExtractionResult(curated, curation.extractionEvaluation)
        }
    }

    private fun extractCategory(
        patientId: String, rawInputs: List<String>, category: CurationCategory, configType: String
    ): List<CurationResponse<ComorbidityConfig>> = rawInputs.map {
        CurationUtil.fullTrim(it).let { input ->
            CurationResponse.createFromConfigs(comorbidityCuration.find(input), patientId, category, input, configType)
        }
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) =
            ComorbidityExtractor(comorbidityCuration = curationDatabaseContext.comorbidityCuration)
    }
}