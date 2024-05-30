package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition

class StandardPriorOtherConditionsExtractor(
    private val priorOtherConditionsCuration: CurationDatabase<NonOncologicalHistoryConfig>,
    private val oncologicalHistoryCuration: CurationDatabase<TreatmentHistoryEntryConfig>
) :
    StandardDataExtractor<List<PriorOtherCondition>> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<PriorOtherCondition>> {
        return ehrPatientRecord.priorOtherConditions.filter { oncologicalHistoryCuration.find(it.name).all(CurationConfig::ignore) }.map {
            val curatedPriorOtherCondition = CurationResponse.createFromConfigs(
                priorOtherConditionsCuration.find(it.name),
                ehrPatientRecord.patientDetails.hashedId,
                CurationCategory.NON_ONCOLOGICAL_HISTORY,
                it.name,
                "non-oncological history",
                false
            )
            ExtractionResult(
                curatedPriorOtherCondition.configs.mapNotNull { config ->
                    config.priorOtherCondition?.copy(
                        year = it.startDate.year,
                        month = it.startDate.monthValue
                    )
                },
                curatedPriorOtherCondition.extractionEvaluation
            )
        }.fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }
}