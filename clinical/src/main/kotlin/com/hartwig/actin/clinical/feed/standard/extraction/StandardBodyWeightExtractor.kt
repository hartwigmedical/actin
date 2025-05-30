package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.feed.emc.EmcClinicalFeedIngestor.Companion.BODY_WEIGHT_EXPECTED_UNIT
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.BodyWeight
import com.hartwig.feed.datamodel.FeedPatientRecord


class StandardBodyWeightExtractor : StandardDataExtractor<List<BodyWeight>> {
    override fun extract(feedPatientRecord: FeedPatientRecord): ExtractionResult<List<BodyWeight>> {
        return ExtractionResult(feedPatientRecord.measurements
            .filter {
                enumeratedInput<MeasurementCategory>(it.category) == MeasurementCategory.BODY_WEIGHT
            }.map {
                BodyWeight(
                    value = it.value,
                    date = it.date.atStartOfDay(),
                    unit = if (enumeratedInput<MeasurementUnit>(it.unit) == MeasurementUnit.KILOGRAMS) "Kilograms" else throw IllegalArgumentException(
                        "Unit of body weight is not Kilograms"
                    ),
                    valid = it.value in 20.0..250.0
                            && BODY_WEIGHT_EXPECTED_UNIT.any { expectedUnit -> expectedUnit.equals(it.unit, ignoreCase = true) }
                )
            }, CurationExtractionEvaluation()
        )
    }
}