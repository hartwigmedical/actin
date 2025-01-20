package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.standard.ProvidedPatientRecord
import com.hartwig.actin.datamodel.clinical.OtherCondition

class StandardOtherConditionsExtractor(
    private val otherConditionsCuration: CurationDatabase<NonOncologicalHistoryConfig>
) :
    StandardDataExtractor<List<OtherCondition>> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<OtherCondition>> {
        return ehrPatientRecord.priorOtherConditions.map {
            val curatedOtherCondition = CurationResponse.createFromConfigs(
                otherConditionsCuration.find(it.name),
                ehrPatientRecord.patientDetails.hashedId,
                CurationCategory.NON_ONCOLOGICAL_HISTORY,
                it.name,
                "non-oncological history",
                false
            )
            ExtractionResult(
                curatedOtherCondition.configs.mapNotNull { config ->
                    it.startDate?.let { sourceStartDate ->
                        config.otherCondition?.copy(
                            year = curatedOtherCondition.config()?.otherCondition?.year ?: sourceStartDate.year,
                            month = curatedOtherCondition.config()?.otherCondition?.month ?: sourceStartDate.monthValue
                        )
                    } ?: config.otherCondition
                },
                curatedOtherCondition.extractionEvaluation
            )
        }.fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }
}