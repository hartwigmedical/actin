package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test

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

    @Test
    fun `Should return converted value with note when conversion factor exists`() {
        val labValue = LabTestFactory.create(LabMeasurement.ALBUMIN, value = 4.0)
            .copy(unit = LabUnit.GRAMS_PER_DECILITER, refLimitLow = 3.5, refLimitUp = 5.0)
        val result = LabUnitConverter.normalizeLabValue(LabMeasurement.ALBUMIN, labValue)!!
        assertThat(result.value.value).isEqualTo(40.0)
        assertThat(result.value.unit).isEqualTo(LabUnit.GRAMS_PER_LITER)
        assertThat(result.value.refLimitLow).isEqualTo(35.0)
        assertThat(result.value.refLimitUp).isEqualTo(50.0)
        assertThat(result.conversionNote).isNotNull()
    }

    @Test
    fun `Should return value with no note when unit already matches default`() {
        val labValue = LabTestFactory.create(LabMeasurement.ALBUMIN, value = 40.0)
        val result = LabUnitConverter.normalizeLabValue(LabMeasurement.ALBUMIN, labValue)!!
        assertThat(result.value).isEqualTo(labValue)
        assertThat(result.conversionNote).isNull()
    }

    @Test
    fun `Should return null when no conversion factor exists for lab value`() {
        val labValue = LabTestFactory.create().copy(unit = LabUnit.CELLS_PER_CUBIC_MILLIMETER)
        assertThat(LabUnitConverter.normalizeLabValue(LabMeasurement.NEUTROPHILS_ABS, labValue)).isNull()
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