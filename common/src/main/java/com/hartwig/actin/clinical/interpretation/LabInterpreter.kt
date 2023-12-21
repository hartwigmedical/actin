package com.hartwig.actin.clinical.interpretation

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Multimap
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue
import com.hartwig.actin.clinical.datamodel.LabValue

object LabInterpreter {
    @JvmField
    val MAPPINGS: MutableMap<LabMeasurement, LabMeasurement> = Maps.newHashMap()

    init {
        MAPPINGS[LabMeasurement.INTERNATIONAL_NORMALIZED_RATIO_POCT] = LabMeasurement.INTERNATIONAL_NORMALIZED_RATIO
        MAPPINGS[LabMeasurement.LYMPHOCYTES_ABS_EDM] = LabMeasurement.LYMPHOCYTES_ABS_EDA
        MAPPINGS[LabMeasurement.NEUTROPHILS_ABS_EDA] = LabMeasurement.NEUTROPHILS_ABS
        MAPPINGS[LabMeasurement.PROTHROMBIN_TIME_POCT] = LabMeasurement.PROTHROMBIN_TIME
        MAPPINGS[LabMeasurement.THROMBOCYTES_ABS_M] = LabMeasurement.THROMBOCYTES_ABS
    }

    fun interpret(labValues: List<LabValue>): LabInterpretation {
        val measurements: Multimap<LabMeasurement, LabValue> = ArrayListMultimap.create()
        for (measurement in LabMeasurement.values()) {
            measurements.putAll(measurement, filterByCode(labValues, measurement.code()))
        }
        for ((key, value) in MAPPINGS) {
            for (labValue in measurements[key]) {
                measurements.put(value, convert(labValue, value))
            }
        }
        return LabInterpretation.Companion.fromMeasurements(measurements)
    }

    private fun convert(labValue: LabValue, targetMeasure: LabMeasurement): LabValue {
        return ImmutableLabValue.builder().from(labValue).code(targetMeasure.code()).unit(targetMeasure.defaultUnit()).build()
    }

    private fun filterByCode(labValues: List<LabValue>, code: String): List<LabValue> {
        val filtered: MutableList<LabValue> = Lists.newArrayList()
        for (labValue in labValues) {
            if (labValue.code() == code) {
                filtered.add(labValue)
            }
        }
        return filtered
    }
}
