package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.apache.logging.log4j.LogManager

internal object LabUnitConverter {
    private val LOGGER = LogManager.getLogger(LabUnitConverter::class.java)
    fun convert(measurement: LabMeasurement, labValue: LabValue, targetUnit: LabUnit): Double? {
        if (labValue.unit == targetUnit) {
            return labValue.value
        }
        val conversionFactor = LabUnitConversionTable.findConversionFactor(measurement, labValue.unit, targetUnit)
        if (conversionFactor == null) {
            LOGGER.warn(
                "No conversion factor defined for {} to go from '{}' to '{}'", measurement.display, labValue.unit, targetUnit
            )
            return null
        }
        return labValue.value * conversionFactor
    }
}