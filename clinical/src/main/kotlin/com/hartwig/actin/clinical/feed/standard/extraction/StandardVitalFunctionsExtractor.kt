package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.VitalFunction
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory
import com.hartwig.feed.datamodel.FeedMeasurement
import com.hartwig.feed.datamodel.FeedPatientRecord

class StandardVitalFunctionsExtractor : StandardDataExtractor<List<VitalFunction>> {
    override fun extract(feedPatientRecord: FeedPatientRecord): ExtractionResult<List<VitalFunction>> {
        return ExtractionResult(feedPatientRecord.measurements.filter {
            !setOf(
                MeasurementCategory.BMI,
                MeasurementCategory.BODY_HEIGHT,
                MeasurementCategory.BODY_WEIGHT
            ).contains(enumeratedInput<MeasurementCategory>(it.category))
        }.map {
            VitalFunction(
                date = it.date.atStartOfDay(),
                category = mapCategory(it),
                subcategory = mapSubcategory(it),
                value = it.value,
                unit = it.unit,
                valid = true
            )
        }, CurationExtractionEvaluation())
    }

    private fun mapCategory(it: FeedMeasurement): VitalFunctionCategory {
        return when (enumeratedInput<MeasurementCategory>(it.category)) {
            MeasurementCategory.ARTERIAL_BLOOD_PRESSURE -> VitalFunctionCategory.ARTERIAL_BLOOD_PRESSURE
            MeasurementCategory.`NON-INVASIVE_BLOOD_PRESSURE` -> VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE
            MeasurementCategory.HEART_RATE -> VitalFunctionCategory.HEART_RATE
            MeasurementCategory.PULSE_OXIMETRY -> VitalFunctionCategory.SPO2
            MeasurementCategory.OTHER -> VitalFunctionCategory.OTHER
            else -> throw IllegalArgumentException("Unknown vital function category ${it.category}")
        }
    }

    private fun mapSubcategory(it: FeedMeasurement): String {
        return when (enumeratedInput<MeasurementSubcategory>(it.subcategory ?: "NA")) {
            MeasurementSubcategory.MEAN_BLOOD_PRESSURE -> "mean blood pressure"
            MeasurementSubcategory.DIASTOLIC_BLOOD_PRESSURE -> "diastolic blood pressure"
            MeasurementSubcategory.SYSTOLIC_BLOOD_PRESSURE -> "systolic blood pressure"
            else -> it.subcategory ?: "NA"
        }
    }
}