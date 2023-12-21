package com.hartwig.actin.clinical.sort

import com.google.common.collect.Lists
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue
import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.datamodel.LabValue
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class LabValueDescendingDateComparatorTest {
    @Test
    fun canSortLabValues() {
        val builder = ImmutableLabValue.builder().name(Strings.EMPTY).comparator(Strings.EMPTY).unit(LabUnit.NONE)
        val value1: LabValue = builder.date(LocalDate.of(2018, 1, 1)).code("Y").value(0.0).build()
        val value2: LabValue = builder.date(LocalDate.of(2019, 1, 1)).code("X").value(0.0).build()
        val value3: LabValue = builder.date(LocalDate.of(2018, 1, 1)).code("X").value(1.0).build()
        val value4: LabValue = builder.date(LocalDate.of(2018, 1, 1)).code("X").value(2.0).build()
        val values: List<LabValue> = Lists.newArrayList(value1, value2, value3, value4)
        values.sort(LabValueDescendingDateComparator())
        Assert.assertEquals(value2, values[0])
        Assert.assertEquals(value4, values[1])
        Assert.assertEquals(value3, values[2])
        Assert.assertEquals(value1, values[3])
    }
}