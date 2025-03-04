package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit

internal object LabUnitConversionTable {

    val CONVERSION_MAP: Map<LabMeasurement, Map<LabUnit, Map<LabUnit, Double>>> = mapOf(
        LabMeasurement.CREATININE to createCreatinineConversionMap(),
        LabMeasurement.ALBUMIN to createAlbuminConversionMap(),
        LabMeasurement.LYMPHOCYTES_ABS to createLymphocytesConversionMap(),
        LabMeasurement.HEMOGLOBIN to createHemoglobinConversionMap(),
        LabMeasurement.CALCIUM to createCalciumConversionMap(),
        LabMeasurement.TOTAL_BILIRUBIN to createBilirubinConversionMap(),
        LabMeasurement.TESTOSTERONE to createTestosteroneConversionMap()
    )

    fun findConversionFactor(measurement: LabMeasurement, fromUnit: LabUnit, toUnit: LabUnit): Double? {
        val measurementMap = CONVERSION_MAP[measurement] ?: return null
        val unitMap = measurementMap[fromUnit]
        return unitMap?.get(toUnit)
    }

    private fun createConversionMap(fromUnit: LabUnit, toUnit: LabUnit, conversionFactor: Double): Map<LabUnit, Map<LabUnit, Double>> {
        return mapOf(
            fromUnit to mapOf(toUnit to conversionFactor),
            toUnit to mapOf(fromUnit to 1 / conversionFactor)
        )
    }

    private fun createCreatinineConversionMap(): Map<LabUnit, Map<LabUnit, Double>> {
        return createConversionMap(LabUnit.MILLIGRAMS_PER_DECILITER, LabUnit.MICROMOLES_PER_LITER, 88.42)
    }

    private fun createAlbuminConversionMap(): Map<LabUnit, Map<LabUnit, Double>> {
        return createConversionMap(LabUnit.GRAMS_PER_DECILITER, LabUnit.GRAMS_PER_LITER, 10.0)
    }

    private fun createLymphocytesConversionMap(): Map<LabUnit, Map<LabUnit, Double>> {
        return createConversionMap(LabUnit.CELLS_PER_CUBIC_MILLIMETER, LabUnit.BILLIONS_PER_LITER, 0.001)
    }

    private fun createHemoglobinConversionMap(): Map<LabUnit, Map<LabUnit, Double>> {
        return createConversionMap(LabUnit.GRAMS_PER_DECILITER, LabUnit.MILLIMOLES_PER_LITER, 0.6206)
    }

    private fun createCalciumConversionMap(): Map<LabUnit, Map<LabUnit, Double>> {
        return createConversionMap(LabUnit.MILLIGRAMS_PER_DECILITER, LabUnit.MILLIMOLES_PER_LITER, 0.2495)
    }

    private fun createBilirubinConversionMap(): Map<LabUnit, Map<LabUnit, Double>> {
        return createConversionMap(LabUnit.MICROMOLES_PER_LITER, LabUnit.MILLIGRAMS_PER_DECILITER, 0.0585)
    }

    private fun createTestosteroneConversionMap(): Map<LabUnit, Map<LabUnit, Double>> {
        return createConversionMap(LabUnit.NANOMOLES_PER_LITER, LabUnit.NANOGRAMS_PER_DECILITER, 28.842)
    }
}