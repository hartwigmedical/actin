package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.BodyWeight


class EhrBodyWeightExtractor : EhrExtractor<List<BodyWeight>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<BodyWeight>> {
        return ExtractionResult(ehrPatientRecord.vitalFunctions.filter {
            it.category == EhrMeasurementCategory.BODY_WEIGHT
        }.map {
            BodyWeight(
                value = it.value,
                date = it.date.atStartOfDay(),
                unit = if (it.unit == EhrVitalFunctionUnit.KG) "KG" else throw IllegalArgumentException("Unit of body weight is not KG"),
                valid = true
            )
        }, ExtractionEvaluation())
    }
}