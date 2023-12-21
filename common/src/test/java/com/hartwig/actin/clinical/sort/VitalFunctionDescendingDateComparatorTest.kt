package com.hartwig.actin.clinical.sort

import com.google.common.collect.Lists
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class VitalFunctionDescendingDateComparatorTest {
    @Test
    fun canSortVitalFunctions() {
        val vitalFunction1 = withDate(LocalDate.of(2020, 1, 1))
        val vitalFunction2 = withDate(LocalDate.of(2019, 1, 1))
        val vitalFunction3 = withDate(LocalDate.of(2018, 1, 1))
        val vitalFunctions: List<VitalFunction> = Lists.newArrayList(vitalFunction3, vitalFunction1, vitalFunction2)
        vitalFunctions.sort(VitalFunctionDescendingDateComparator())
        Assert.assertEquals(vitalFunction1, vitalFunctions[0])
        Assert.assertEquals(vitalFunction2, vitalFunctions[1])
        Assert.assertEquals(vitalFunction3, vitalFunctions[2])
    }

    companion object {
        private fun withDate(date: LocalDate): VitalFunction {
            return ImmutableVitalFunction.builder()
                .date(date)
                .category(VitalFunctionCategory.HEART_RATE)
                .subcategory(Strings.EMPTY)
                .value(0.0)
                .unit(Strings.EMPTY)
                .build()
        }
    }
}