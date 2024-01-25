package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition

class EhrPriorOtherConditionsExtractor : EhrExtractor<List<PriorOtherCondition>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<PriorOtherCondition>> {
        return ExtractionResult(ehrPatientRecord.priorOtherConditions.map {
            ImmutablePriorOtherCondition.builder().name(it.diagnosis).year(it.startDate.year).month(it.startDate.monthValue).category("")
                .isContraindicationForTherapy(false).build()
        }, ExtractionEvaluation())
    }
}