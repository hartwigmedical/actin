package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.provided.ProvidedMeasurement
import com.hartwig.actin.datamodel.clinical.provided.ProvidedMeasurementCategory
import com.hartwig.actin.datamodel.clinical.provided.ProvidedMeasurementSubcategory
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientRecord
import com.hartwig.actin.datamodel.clinical.VitalFunction
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory

class StandardVitalFunctionsExtractor : StandardDataExtractor<List<VitalFunction>> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<VitalFunction>> {
        return ExtractionResult(ehrPatientRecord.measurements.filter {
            !setOf(
                ProvidedMeasurementCategory.BMI,
                ProvidedMeasurementCategory.BODY_HEIGHT,
                ProvidedMeasurementCategory.BODY_WEIGHT
            ).contains(enumeratedInput<ProvidedMeasurementCategory>(it.category))
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

    private fun mapCategory(it: ProvidedMeasurement): VitalFunctionCategory {
        return when (enumeratedInput<ProvidedMeasurementCategory>(it.category)) {
            ProvidedMeasurementCategory.ARTERIAL_BLOOD_PRESSURE -> VitalFunctionCategory.ARTERIAL_BLOOD_PRESSURE
            ProvidedMeasurementCategory.`NON-INVASIVE_BLOOD_PRESSURE` -> VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE
            ProvidedMeasurementCategory.HEART_RATE -> VitalFunctionCategory.HEART_RATE
            ProvidedMeasurementCategory.PULSE_OXIMETRY -> VitalFunctionCategory.SPO2
            ProvidedMeasurementCategory.OTHER -> VitalFunctionCategory.OTHER
            else -> throw IllegalArgumentException("Unknown vital function category ${it.category}")
        }
    }

    private fun mapSubcategory(it: ProvidedMeasurement): String {
        return when (enumeratedInput<ProvidedMeasurementSubcategory>(it.subcategory ?: "NA")) {
            ProvidedMeasurementSubcategory.MEAN_BLOOD_PRESSURE -> "mean blood pressure"
            ProvidedMeasurementSubcategory.DIASTOLIC_BLOOD_PRESSURE -> "diastolic blood pressure"
            ProvidedMeasurementSubcategory.SYSTOLIC_BLOOD_PRESSURE -> "systolic blood pressure"
            else -> it.subcategory ?: "NA"
        }
    }
}