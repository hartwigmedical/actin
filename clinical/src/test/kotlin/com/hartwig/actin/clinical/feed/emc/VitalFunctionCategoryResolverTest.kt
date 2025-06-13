package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory
import org.assertj.core.api.Assertions
import org.junit.Test

class VitalFunctionCategoryResolverTest {

    @Test
    fun `Should determine vital function category`() {
        Assertions.assertThat(VitalFunctionCategoryResolver.determineCategory("NIBP"))
            .isEqualTo(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE)
        Assertions.assertThat(VitalFunctionCategoryResolver.determineCategory("ABP"))
            .isEqualTo(VitalFunctionCategory.ARTERIAL_BLOOD_PRESSURE)
        Assertions.assertThat(VitalFunctionCategoryResolver.determineCategory("HR")).isEqualTo(VitalFunctionCategory.HEART_RATE)
        Assertions.assertThat(VitalFunctionCategoryResolver.determineCategory("SpO2")).isEqualTo(VitalFunctionCategory.SPO2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception on invalid vital function category`() {
        VitalFunctionCategoryResolver.determineCategory("not a category")
    }
}