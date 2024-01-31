package com.hartwig.actin.clinical.ehr

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition

class EhrPriorOtherConditionsExtractor(private val priorOtherConditionsCuration: CurationDatabase<NonOncologicalHistoryConfig>) :
    EhrExtractor<List<PriorOtherCondition>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<PriorOtherCondition>> {
        return ehrPatientRecord.priorOtherConditions.map {
            val curatedPriorOtherCondition = CurationResponse.createFromConfigs(
                priorOtherConditionsCuration.find(it.name),
                ehrPatientRecord.patientDetails.patientId,
                CurationCategory.NON_ONCOLOGICAL_HISTORY,
                it.name,
                "non-oncological history"
            )
            ExtractionResult(
                listOfNotNull(
                    curatedPriorOtherCondition.config()?.priorOtherCondition?.copy(
                        year = it.startDate.year,
                        month = it.startDate.monthValue
                    )
                ), curatedPriorOtherCondition.extractionEvaluation
            )
        }.fold(ExtractionResult(emptyList(), ExtractionEvaluation())) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }
}