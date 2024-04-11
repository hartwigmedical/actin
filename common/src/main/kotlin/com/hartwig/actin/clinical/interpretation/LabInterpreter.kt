package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.clinical.datamodel.LabValue

object LabInterpreter {

    val MAPPINGS: Map<LabMeasurement, LabMeasurement> = mapOf(
        LabMeasurement.INTERNATIONAL_NORMALIZED_RATIO_POCT to LabMeasurement.INTERNATIONAL_NORMALIZED_RATIO,
        LabMeasurement.LYMPHOCYTES_ABS_EDM to LabMeasurement.LYMPHOCYTES_ABS_EDA,
        LabMeasurement.NEUTROPHILS_ABS_EDA to LabMeasurement.NEUTROPHILS_ABS,
        LabMeasurement.PROTHROMBIN_TIME_POCT to LabMeasurement.PROTHROMBIN_TIME,
        LabMeasurement.THROMBOCYTES_ABS_M to LabMeasurement.THROMBOCYTES_ABS,
        LabMeasurement.BOUND_T3_ATEL to LabMeasurement.BOUND_T3,
        LabMeasurement.FREE_T3_ATEL to LabMeasurement.FREE_T3,
        LabMeasurement.FREE_T4_ATEL to LabMeasurement.FREE_T4,
        LabMeasurement.THYROID_STIMULATING_HORMONE_ATEL_SCREENING to LabMeasurement.THYROID_STIMULATING_HORMONE,
        LabMeasurement.THYROID_STIMULATING_HORMONE_ATEL to LabMeasurement.THYROID_STIMULATING_HORMONE
    )
    
    fun interpret(labValues: List<LabValue>): LabInterpretation {
        val labValuesByCode = labValues.groupBy(LabValue::code)
        val baseMeasurements = LabMeasurement.values().associateWith { labValuesByCode[it.code] ?: emptyList() }
        val mappedMeasurements = MAPPINGS.entries.associate { (fromMeasurement, toMeasurement) ->
            toMeasurement to baseMeasurements[fromMeasurement]!!.map { convert(it, toMeasurement) }
        }

        val allMeasurements = baseMeasurements.mapValues { (measurement, values) ->
            values + (mappedMeasurements[measurement] ?: emptyList())
        }
        return LabInterpretation.fromMeasurements(allMeasurements)
    }

    private fun convert(labValue: LabValue, targetMeasure: LabMeasurement): LabValue {
        return labValue.copy(code = targetMeasure.code, unit = targetMeasure.defaultUnit)
    }
}
