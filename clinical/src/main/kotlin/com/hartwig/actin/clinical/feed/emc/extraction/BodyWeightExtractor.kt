package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.BodyWeight
import com.hartwig.feed.datamodel.FeedMeasurement
import com.hartwig.feed.datamodel.FeedPatientRecord


class BodyWeightExtractor {

    fun extract(feedRecord: FeedPatientRecord): ExtractionResult<List<BodyWeight>> {

        return ExtractionResult(
            feedRecord.measurements
                .filter { it.category == "BODY_WEIGHT" }
                .filter { it.value > 0 }
                .map { entry ->
                    BodyWeight(
                        date = entry.date,
                        value = entry.value,
                        unit = entry.unit,
                        valid = bodyWeightIsValid(entry)
                    )
                }, CurationExtractionEvaluation()
        )
    }

    private fun bodyWeightIsValid(entry: FeedMeasurement): Boolean {
        return entry.unit.lowercase() in BODY_WEIGHT_EXPECTED_UNIT && entry.value in BODY_WEIGHT_MIN..BODY_WEIGHT_MAX
    }

    companion object {
        internal val BODY_WEIGHT_EXPECTED_UNIT = listOf("kilogram", "kilograms")
        const val BODY_WEIGHT_MIN = 20.0
        const val BODY_WEIGHT_MAX = 300.0
    }
}