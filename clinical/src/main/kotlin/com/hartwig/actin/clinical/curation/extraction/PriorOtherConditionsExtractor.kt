package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfigFactory
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.clinical.feed.questionnaire.Questionnaire

class PriorOtherConditionsExtractor(private val nonOncologicalCuration: CurationDatabase<NonOncologicalHistoryConfig>) {

    fun extract(patientId: String, questionnaire: Questionnaire?): ExtractionResult<List<PriorOtherCondition>> {
        if (questionnaire?.nonOncologicalHistory == null) {
            return ExtractionResult(emptyList(), ExtractionEvaluation())
        }
        val curation = questionnaire.nonOncologicalHistory
            .asSequence()
            .map(CurationUtil::fullTrim)
            .map {
                CurationResponse.createFromConfigs(
                    nonOncologicalCuration.curate(it),
                    patientId,
                    CurationCategory.NON_ONCOLOGICAL_HISTORY,
                    it,
                    "non-oncological history"
                )
            }
            .fold(CurationResponse<NonOncologicalHistoryConfig>()) { acc, cur -> acc + cur }

        return ExtractionResult(curation.configs.mapNotNull { it.priorOtherCondition }, curation.extractionEvaluation)
    }

    companion object {
        fun create(curationDir: String, curationDoidValidator: CurationDoidValidator) =
            PriorOtherConditionsExtractor(
                nonOncologicalCuration = CurationDatabaseReader.read(
                    curationDir,
                    CurationDatabaseReader.NON_ONCOLOGICAL_HISTORY_TSV,
                    NonOncologicalHistoryConfigFactory(curationDoidValidator)
                )
            )
    }
}