package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.hartwig.actin.clinical.datamodel.LabUnit;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class LabUnitConverterTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canConvert() {
        LabMeasurement measurement = firstMeasurementWithConversionTable();
        LabUnit firstFromKey = firstFromUnitConversionFactorKey();
        LabUnit firstToKey = firstToUnitConversionFactorKey();
        double conversionFactor = firstConversionFactor();

        LabValue value = LabTestFactory.builder().unit(firstToKey).value(conversionFactor).build();

        assertEquals(conversionFactor, LabUnitConverter.convert(measurement, value, firstToKey), EPSILON);
        assertEquals(1D, LabUnitConverter.convert(measurement, value, firstFromKey), EPSILON);
    }

    @Test
    public void missingConversionEntryLeadsToNull() {
        LabValue value = LabTestFactory.builder().unit(LabUnit.CELLS_PER_CUBIC_MILLIMETER).build();
        assertNull(LabUnitConverter.convert(LabMeasurement.NEUTROPHILS_ABS, value, LabUnit.GRAMS_PER_LITER));
    }

    @NotNull
    private static LabMeasurement firstMeasurementWithConversionTable() {
        return LabUnitConversionTable.CONVERSION_MAP.keySet().iterator().next();
    }

    @NotNull
    private static LabUnit firstFromUnitConversionFactorKey() {
        return LabUnitConversionTable.CONVERSION_MAP.get(firstMeasurementWithConversionTable()).keySet().iterator().next();
    }

    @NotNull
    private static LabUnit firstToUnitConversionFactorKey() {
        return LabUnitConversionTable.CONVERSION_MAP.get(firstMeasurementWithConversionTable())
                .get(firstFromUnitConversionFactorKey())
                .keySet()
                .iterator()
                .next();
    }

    private static double firstConversionFactor() {
        return LabUnitConversionTable.CONVERSION_MAP.get(firstMeasurementWithConversionTable())
                .get(firstFromUnitConversionFactorKey())
                .get(firstToUnitConversionFactorKey());
    }
}