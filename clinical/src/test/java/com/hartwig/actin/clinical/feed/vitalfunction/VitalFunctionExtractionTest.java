package com.hartwig.actin.clinical.feed.vitalfunction;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory;

import org.junit.Test;

public class VitalFunctionExtractionTest {

    @Test
    public void canDetermineVitalFunctionCategory() {
        assertEquals(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE, VitalFunctionExtraction.determineCategory("NIBP"));
        assertEquals(VitalFunctionCategory.ARTERIAL_BLOOD_PRESSURE, VitalFunctionExtraction.determineCategory("ABP"));
        assertEquals(VitalFunctionCategory.HEART_RATE, VitalFunctionExtraction.determineCategory("HR"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashOnInvalidVitalFunctionCategory() {
        VitalFunctionExtraction.determineCategory("not a category");
    }
}