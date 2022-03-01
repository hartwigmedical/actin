package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.hartwig.actin.clinical.datamodel.LabValue;

import org.junit.Test;

public class LabUnitConverterTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canConvert() {
        LabValue value = LabTestFactory.builder().unit(LabUnit.MICROMOL_PER_LITER.display()).value(88.42).build();

        assertEquals(88.42, LabUnitConverter.convert(value, LabUnit.MICROMOL_PER_LITER), EPSILON);
        assertEquals(1D, LabUnitConverter.convert(value, LabUnit.MILLIGRAM_PER_DECILITER), EPSILON);
    }

    @Test
    public void unrecognizedUnitLeadsToNull() {
        assertNull(LabUnitConverter.convert(LabTestFactory.builder().unit("not a unit").build(), LabUnit.GRAM_PER_LITER));
    }

    @Test
    public void missingConversionEntryLeadsToNull() {
        LabValue value = LabTestFactory.builder().unit(LabUnit.CELLS_PER_MICROLITER.display()).build();
        assertNull(LabUnitConverter.convert(value, LabUnit.GRAM_PER_LITER));
    }
}