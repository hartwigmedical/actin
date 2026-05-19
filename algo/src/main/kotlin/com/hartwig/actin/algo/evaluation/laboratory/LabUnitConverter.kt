package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.datamodel.clinical.LabValue
import io.github.oshai.kotlinlogging.KotlinLogging

internal object LabUnitConverter {

    private val logger = KotlinLogging.logger {}

    fun convert(measurement: LabMeasurement, labValue: LabValue, targetUnit: LabUnit): Double? {
        if (labValue.unit == targetUnit) {
            return labValue.value
        }
        val conversionFactor = LabUnitConversionTable.findConversionFactor(measurement, labValue.unit, targetUnit)
        if (conversionFactor == null) {
            logger.warn { "No conversion factor defined for ${measurement.display} to go from '${labValue.unit.display()}' to '$targetUnit'" }
            return null
        }
        return labValue.value * conversionFactor
    }

    data class NormalizedLabValue(val value: LabValue, val conversionNote: String? = null)

    fun normalizeLabValue(measurement: LabMeasurement, labValue: LabValue): NormalizedLabValue? {
        val targetUnit = measurement.defaultUnit
        val conversionFactor = LabUnitConversionTable.findConversionFactor(measurement, labValue.unit, targetUnit)
        return when {
            labValue.unit == targetUnit -> NormalizedLabValue(labValue)
            conversionFactor == null -> {
                logger.warn { "No conversion factor defined for ${measurement.display()} to go from '${labValue.unit.display()}' to '$targetUnit'" }
                null
            }
            else -> {
                val converted = labValue.copy(
                    value = labValue.value * conversionFactor,
                    unit = targetUnit,
                    refLimitLow = labValue.refLimitLow?.let { it * conversionFactor },
                    refLimitUp = labValue.refLimitUp?.let { it * conversionFactor }
                )
                NormalizedLabValue(
                    value = converted,
                    conversionNote = "${measurement.display()} converted from: ${labValue.value} ${labValue.unit.display()} to ${converted.value} ${converted.unit.display()}"
                )
            }
        }
    }
}
