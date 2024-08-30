package com.hartwig.actin.clinical.feed.emc.vitalfunction

import com.hartwig.actin.clinical.feed.emc.vitalfunction.VitalFunctionExtraction.determineCategory
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory
import org.junit.Assert
import org.junit.Test

class VitalFunctionExtractionTest {
    @Test
    fun canDetermineVitalFunctionCategory() {
        Assert.assertEquals(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE, determineCategory("NIBP"))
        Assert.assertEquals(VitalFunctionCategory.ARTERIAL_BLOOD_PRESSURE, determineCategory("ABP"))
        Assert.assertEquals(VitalFunctionCategory.HEART_RATE, determineCategory("HR"))
        Assert.assertEquals(VitalFunctionCategory.SPO2, determineCategory("SpO2"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun crashOnInvalidVitalFunctionCategory() {
        determineCategory("not a category")
    }
}