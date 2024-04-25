package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.feed.emc.EmcClinicalFeedIngestor.Companion.BODY_WEIGHT_EXPECTED_UNIT


class EhrBodyWeightExtractor : EhrExtractor<List<BodyWeight>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<BodyWeight>> {
        return ExtractionResult(ehrPatientRecord.measurements
            .filter {
                enumeratedInput<EhrMeasurementCategory>(it.category) == EhrMeasurementCategory.BODY_WEIGHT
            }.map {
                BodyWeight(
                    value = it.value,
                    date = it.date.atStartOfDay(),
                    unit = if (enumeratedInput<EhrMeasurementUnit>(it.unit) == EhrMeasurementUnit.KILOGRAMS) "Kilograms" else throw IllegalArgumentException(
                        "Unit of body weight is not Kilograms"
                    ),
                    valid = it.value in 20.0..250.0 && it.unit in BODY_WEIGHT_EXPECTED_UNIT
                )
            }, CurationExtractionEvaluation()
        )
    }
}