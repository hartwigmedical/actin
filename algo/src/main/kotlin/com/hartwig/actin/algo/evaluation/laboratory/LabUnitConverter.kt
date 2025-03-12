package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.datamodel.clinical.LabValue
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
                "No conversion factor defined for {} to go from '{}' to '{}'", measurement.display, labValue.unit.display(), targetUnit
            )
            return null
        }
        return labValue.value * conversionFactor
    }
}