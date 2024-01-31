package com.hartwig.actin.clinical.ehr

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.BodyWeight


class EhrBodyWeightExtractor : EhrExtractor<List<BodyWeight>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<BodyWeight>> {
        return ExtractionResult(ehrPatientRecord.measurements.filter {
            it.category.acceptedValues == EhrMeasurementCategory.BODY_WEIGHT
        }.map {
            BodyWeight(
                value = it.value,
                date = it.date.atStartOfDay(),
                unit = if (it.unit.acceptedValues == EhrMeasurementUnit.KG) "KG" else throw IllegalArgumentException("Unit of body weight is not KG"),
                valid = true
            )
        }, ExtractionEvaluation())
    }
}