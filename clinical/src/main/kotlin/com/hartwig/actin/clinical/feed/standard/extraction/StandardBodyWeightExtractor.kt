package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.BodyWeight
import com.hartwig.feed.datamodel.FeedPatientRecord


class StandardBodyWeightExtractor : StandardDataExtractor<List<BodyWeight>> {
    override fun extract(feedPatientRecord: FeedPatientRecord): ExtractionResult<List<BodyWeight>> {
        return ExtractionResult(
            extracted = feedPatientRecord.measurements
                .filter { enumeratedInput<MeasurementCategory>(it.category) == MeasurementCategory.BODY_WEIGHT }
                .filter { it.value > 0 }
                .map { meas ->
                    BodyWeight(
                        value = meas.value,
                        date = meas.date,
                        unit = meas.unit.takeIf { meas.unit.lowercase() in BODY_WEIGHT_EXPECTED_UNIT }
                            ?: throw IllegalArgumentException("Unit of body weight is not Kilograms"),
                        valid = meas.value in BODY_WEIGHT_MIN..BODY_WEIGHT_MAX
                    )
                },
            evaluation = CurationExtractionEvaluation()
        )
    }

    companion object {
        internal val BODY_WEIGHT_EXPECTED_UNIT = listOf("kilogram", "kilograms")
        const val BODY_WEIGHT_MIN = 20.0
        const val BODY_WEIGHT_MAX = 300.0
    }
}