package com.hartwig.actin.clinical.ehr

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory

class EhrVitalFunctionsExtractor : EhrExtractor<List<VitalFunction>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<VitalFunction>> {
        return ExtractionResult(ehrPatientRecord.measurements.filter {
            !setOf(
                EhrMeasurementCategory.BMI,
                EhrMeasurementCategory.BODY_HEIGHT,
                EhrMeasurementCategory.BODY_WEIGHT
            ).contains(enumeratedInput<EhrMeasurementCategory>(it.category))
        }.map {
            VitalFunction(
                date = it.date.atStartOfDay(),
                category = mapCategory(it),
                subcategory = mapSubcategory(it),
                value = it.value,
                unit = it.unit,
                valid = true
            )
        }, ExtractionEvaluation())
    }

    private fun mapCategory(it: EhrMeasurement): VitalFunctionCategory {
        return when (enumeratedInput<EhrMeasurementCategory>(it.category)) {
            EhrMeasurementCategory.ARTERIAL_BLOOD_PRESSURE -> VitalFunctionCategory.ARTERIAL_BLOOD_PRESSURE
            EhrMeasurementCategory.`NON-INVASIVE_BLOOD_PRESSURE` -> VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE
            EhrMeasurementCategory.HEART_RATE -> VitalFunctionCategory.HEART_RATE
            EhrMeasurementCategory.PULSE_OXIMETRY -> VitalFunctionCategory.SPO2
            EhrMeasurementCategory.OTHER -> VitalFunctionCategory.OTHER
            else -> throw IllegalArgumentException("Unknown vital function category ${it.category}")
        }
    }

    private fun mapSubcategory(it: EhrMeasurement): String {
        return when (enumeratedInput<EhrMeasurementSubcategory>(it.subcategory ?: "NA")) {
            EhrMeasurementSubcategory.MEAN_BLOOD_PRESSURE -> "mean blood pressure"
            EhrMeasurementSubcategory.DIASTOLIC_BLOOD_PRESSURE -> "diastolic blood pressure"
            EhrMeasurementSubcategory.SYSTOLIC_BLOOD_PRESSURE -> "systolic blood pressure"
            else -> it.subcategory ?: "NA"
        }
    }
}