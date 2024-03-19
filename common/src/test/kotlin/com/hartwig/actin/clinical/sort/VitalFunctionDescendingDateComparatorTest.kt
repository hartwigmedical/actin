package com.hartwig.actin.clinical.sort

import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class VitalFunctionDescendingDateComparatorTest {

    @Test
    fun `Should sort vital functions`() {
        val vitalFunction1 = withDate(LocalDate.of(2020, 1, 1))
        val vitalFunction2 = withDate(LocalDate.of(2019, 1, 1))
        val vitalFunction3 = withDate(LocalDate.of(2018, 1, 1))
        val vitalFunctions = listOf(vitalFunction3, vitalFunction1, vitalFunction2).sortedWith(VitalFunctionDescendingDateComparator())

        assertThat(vitalFunctions).containsExactly(vitalFunction1, vitalFunction2, vitalFunction3)
    }

    private fun withDate(date: LocalDate): VitalFunction {
        return VitalFunction(
            date = date.atStartOfDay(),
            category = VitalFunctionCategory.HEART_RATE,
            subcategory = "",
            value = 0.0,
            unit = "",
            valid = true
        )
    }
}