package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.feed.emc.EmcClinicalFeedIngestor.Companion.BODY_WEIGHT_EXPECTED_UNIT
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.provided.ProvidedMeasurementCategory
import com.hartwig.actin.datamodel.clinical.provided.ProvidedMeasurementUnit
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientRecord
import com.hartwig.actin.datamodel.clinical.BodyWeight


class StandardBodyWeightExtractor : StandardDataExtractor<List<BodyWeight>> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<BodyWeight>> {
        return ExtractionResult(ehrPatientRecord.measurements
            .filter {
                enumeratedInput<ProvidedMeasurementCategory>(it.category) == ProvidedMeasurementCategory.BODY_WEIGHT
            }.map {
                BodyWeight(
                    value = it.value,
                    date = it.date.atStartOfDay(),
                    unit = if (enumeratedInput<ProvidedMeasurementUnit>(it.unit) == ProvidedMeasurementUnit.KILOGRAMS) "Kilograms" else throw IllegalArgumentException(
                        "Unit of body weight is not Kilograms"
                    ),
                    valid = it.value in 20.0..250.0
                            && BODY_WEIGHT_EXPECTED_UNIT.any { expectedUnit -> expectedUnit.equals(it.unit, ignoreCase = true) }
                )
            }, CurationExtractionEvaluation()
        )
    }
}