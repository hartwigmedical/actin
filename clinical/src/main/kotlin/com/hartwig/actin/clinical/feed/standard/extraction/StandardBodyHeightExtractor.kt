package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.BodyHeight
import com.hartwig.feed.datamodel.FeedPatientRecord

class StandardBodyHeightExtractor : StandardDataExtractor<List<BodyHeight>> {
    override fun extract(feedPatientRecord: FeedPatientRecord): ExtractionResult<List<BodyHeight>> {
        return ExtractionResult(feedPatientRecord.measurements
            .filter {
                enumeratedInput<MeasurementCategory>(it.category) == MeasurementCategory.BODY_HEIGHT
            }.map {
                BodyHeight(
                    value = it.value,
                    date = it.date.atStartOfDay(),
                    unit = if (enumeratedInput<MeasurementUnit>(it.unit) == MeasurementUnit.CENTIMETERS) {
                        "centimeters"
                    } else throw IllegalArgumentException("Unit of body height is not centimeters"),
                    valid = it.value in 100.0..250.0
                )
            }, CurationExtractionEvaluation()
        )
    }
}