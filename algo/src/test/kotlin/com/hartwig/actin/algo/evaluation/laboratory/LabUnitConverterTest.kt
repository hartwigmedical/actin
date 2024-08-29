package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test

class LabUnitConverterTest {

    @Test
    fun canConvert() {
        val measurement = firstMeasurementWithConversionTable()
        val firstFromKey = firstFromUnitConversionFactorKey()
        val firstToKey = firstToUnitConversionFactorKey()
        val conversionFactor = firstConversionFactor()
        val value = LabTestFactory.create(value = conversionFactor).copy(unit = firstToKey)
        assertThat(LabUnitConverter.convert(measurement, value, firstToKey)!!).isEqualTo(conversionFactor, Offset.offset(EPSILON))
        assertThat(LabUnitConverter.convert(measurement, value, firstFromKey)!!).isEqualTo(1.0, Offset.offset(EPSILON))
    }

    @Test
    fun missingConversionEntryLeadsToNull() {
        val value = LabTestFactory.create().copy(unit = LabUnit.CELLS_PER_CUBIC_MILLIMETER)
        assertThat(LabUnitConverter.convert(LabMeasurement.NEUTROPHILS_ABS, value, LabUnit.GRAMS_PER_LITER)).isNull()
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