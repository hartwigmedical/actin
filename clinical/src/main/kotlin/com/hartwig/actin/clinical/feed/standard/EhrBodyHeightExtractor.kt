package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.BodyHeight

class EhrBodyHeightExtractor : EhrExtractor<List<BodyHeight>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<BodyHeight>> {
        return ExtractionResult(ehrPatientRecord.measurements
            .filter {
                enumeratedInput<EhrMeasurementCategory>(it.category) == EhrMeasurementCategory.BODY_HEIGHT
            }.map {
                BodyHeight(
                    value = it.value,
                    date = it.date.atStartOfDay(),
                    unit = if (enumeratedInput<EhrMeasurementUnit>(it.unit) == EhrMeasurementUnit.CENTIMETERS) "centimeters" else throw IllegalArgumentException(
                        "Unit of body height is not centimeters"
                    ),
                    valid = it.value in 100.0..250.0
                )
            }, CurationExtractionEvaluation()
        )
    }
}