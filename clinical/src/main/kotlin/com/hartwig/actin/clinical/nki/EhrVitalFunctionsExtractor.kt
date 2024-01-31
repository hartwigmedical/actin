package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory

class EhrVitalFunctionsExtractor : EhrExtractor<List<VitalFunction>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<VitalFunction>> {
        return ExtractionResult(ehrPatientRecord.vitalFunctions.filter {
            !setOf(
                EhrMeasurementCategory.BMI,
                EhrMeasurementCategory.BODY_HEIGHT,
                EhrMeasurementCategory.BODY_WEIGHT
            ).contains(it.category)
        }.map {
            VitalFunction(
                date = it.date.atStartOfDay(),
                category = mapCategory(it),
                subcategory = mapSubcategory(it),
                value = it.value,
                unit = it.unit.name.lowercase(),
                valid = true
            )
        }, ExtractionEvaluation())
    }

    private fun mapCategory(it: EhrMeasurement): VitalFunctionCategory {
        return when (it.category) {
            EhrMeasurementCategory.ARTERIAL_BLOOD_PRESSURE -> VitalFunctionCategory.ARTERIAL_BLOOD_PRESSURE
            EhrMeasurementCategory.NON_INVASIVE_BLOOD_PRESSURE -> VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE
            EhrMeasurementCategory.HEART_RATE -> VitalFunctionCategory.HEART_RATE
            EhrMeasurementCategory.PULSE_OXIMETRY -> VitalFunctionCategory.SPO2
            EhrMeasurementCategory.OTHER -> VitalFunctionCategory.OTHER
            else -> throw IllegalArgumentException("Unknown vital function category ${it.category}")
        }
    }

    private fun mapSubcategory(it: EhrMeasurement): String {
        return when (it.subcategory) {
            EhrMeasurementSubcategory.MEAN_BLOOD_PRESSURE -> "mean blood pressure"
            EhrMeasurementSubcategory.DIASTOLIC_BLOOD_PRESSURE -> "diastolic blood pressure"
            EhrMeasurementSubcategory.SYSTOLIC_BLOOD_PRESSURE -> "systolic blood pressure"
            else -> "N/A"
        }
    }
}