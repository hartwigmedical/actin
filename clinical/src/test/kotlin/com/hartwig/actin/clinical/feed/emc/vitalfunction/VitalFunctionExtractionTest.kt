package com.hartwig.actin.clinical.feed.emc.vitalfunction

import com.hartwig.actin.clinical.feed.emc.vitalfunction.VitalFunctionExtraction.determineCategory
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VitalFunctionExtractionTest {

    @Test
    fun `Should determine vital function category`() {
        assertThat(determineCategory("NIBP")).isEqualTo(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE)
        assertThat(determineCategory("ABP")).isEqualTo(VitalFunctionCategory.ARTERIAL_BLOOD_PRESSURE)
        assertThat(determineCategory("HR")).isEqualTo(VitalFunctionCategory.HEART_RATE)
        assertThat(determineCategory("SpO2")).isEqualTo(VitalFunctionCategory.SPO2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception on invalid vital function category`() {
        determineCategory("not a category")
    }
}