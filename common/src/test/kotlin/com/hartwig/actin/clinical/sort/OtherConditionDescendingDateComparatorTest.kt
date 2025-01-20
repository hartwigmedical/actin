package com.hartwig.actin.clinical.sort

import com.hartwig.actin.datamodel.clinical.TestOtherConditionFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class OtherConditionDescendingDateComparatorTest {

    @Test
    fun `Should sort by descending based on year and month keeping nulls at the end`() {

        val c1 = TestOtherConditionFactory.create("c1", 2023, 3)
        val c2 = TestOtherConditionFactory.create("c2", null, null)
        val c3 = TestOtherConditionFactory.create("c3", 2023, null)
        val c4 = TestOtherConditionFactory.create("c4", null, 2024)
        val c5 = TestOtherConditionFactory.create("c5", 2024, 3)
        val c6 = TestOtherConditionFactory.create("c6", 2023, 5)
        val c7 = TestOtherConditionFactory.create("c7", 2024, 8)

        val conditions = listOf(c1, c2, c3, c4, c5, c6, c7).sortedWith(OtherConditionDescendingDateComparator())

        assertThat(conditions).containsExactly(c7, c5, c6, c1, c3, c4, c2)
    }


}