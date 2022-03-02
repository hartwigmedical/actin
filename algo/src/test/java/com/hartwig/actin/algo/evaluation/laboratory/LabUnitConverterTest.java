package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.hartwig.actin.clinical.datamodel.LabUnit;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class LabUnitConverterTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canConvert() {
        LabMeasurement measurement = LabUnitConversionTable.CONVERSION_MAP.keySet().iterator().next();

        LabUnit firstFromKey = LabUnitConversionTable.CONVERSION_MAP.get(measurement).keySet().iterator().next();
        LabUnit firstToKey = LabUnitConversionTable.CONVERSION_MAP.get(measurement).get(firstFromKey).keySet().iterator().next();
        double conversionFactor = LabUnitConversionTable.CONVERSION_MAP.get(measurement).get(firstFromKey).get(firstToKey);

        LabValue value = LabTestFactory.builder().unit(firstToKey).value(conversionFactor).build();

        assertEquals(conversionFactor, LabUnitConverter.convert(measurement, value, firstToKey), EPSILON);
        assertEquals(1D, LabUnitConverter.convert(measurement, value, firstFromKey), EPSILON);
    }

    @Test
    public void missingConversionEntryLeadsToNull() {
        LabValue value = LabTestFactory.builder().unit(LabUnit.CELLS_PER_CUBIC_MILLIMETER).build();
        assertNull(LabUnitConverter.convert(LabMeasurement.NEUTROPHILS_ABS, value, LabUnit.GRAMS_PER_LITER));
    }
}