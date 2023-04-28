package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.junit.Assert
import org.junit.Test

class LabUnitConverterTest {
    @Test
    fun canConvert() {
        val measurement = firstMeasurementWithConversionTable()
        val firstFromKey = firstFromUnitConversionFactorKey()
        val firstToKey = firstToUnitConversionFactorKey()
        val conversionFactor = firstConversionFactor()
        val value: LabValue = LabTestFactory.builder().unit(firstToKey).value(conversionFactor).build()
        Assert.assertEquals(conversionFactor, LabUnitConverter.convert(measurement, value, firstToKey)!!, EPSILON)
        Assert.assertEquals(1.0, LabUnitConverter.convert(measurement, value, firstFromKey)!!, EPSILON)
    }

    @Test
    fun missingConversionEntryLeadsToNull() {
        val value: LabValue = LabTestFactory.builder().unit(LabUnit.CELLS_PER_CUBIC_MILLIMETER).build()
        Assert.assertNull(LabUnitConverter.convert(LabMeasurement.NEUTROPHILS_ABS, value, LabUnit.GRAMS_PER_LITER))
    }

    companion object {
        private const val EPSILON = 1.0E-10
        private fun firstMeasurementWithConversionTable(): LabMeasurement {
            return LabUnitConversionTable.CONVERSION_MAP.keys.iterator().next()
        }

        private fun firstFromUnitConversionFactorKey(): LabUnit {
            return LabUnitConversionTable.CONVERSION_MAP[firstMeasurementWithConversionTable()]!!.keys.iterator().next()
        }

        private fun firstToUnitConversionFactorKey(): LabUnit {
            return LabUnitConversionTable.CONVERSION_MAP[firstMeasurementWithConversionTable()]!![firstFromUnitConversionFactorKey()]!!
                .keys
                .iterator()
                .next()
        }

        private fun firstConversionFactor(): Double {
            return LabUnitConversionTable.CONVERSION_MAP[firstMeasurementWithConversionTable()]!![firstFromUnitConversionFactorKey()]!![firstToUnitConversionFactorKey()]!!
        }
    }
}