package com.hartwig.actin.clinical.sort

import com.hartwig.actin.datamodel.clinical.TestPriorOtherConditionFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PriorOtherConditionDescendingDateComparatorTest {

    @Test
    fun `Should sort by descending based on year and month keeping nulls at the end`() {

        val c1 = TestPriorOtherConditionFactory.create("c1", 2023, 3)
        val c2 = TestPriorOtherConditionFactory.create("c2", null, null)
        val c3 = TestPriorOtherConditionFactory.create("c3", 2023, null)
        val c4 = TestPriorOtherConditionFactory.create("c4", null, 2024)
        val c5 = TestPriorOtherConditionFactory.create("c5", 2024, 3)
        val c6 = TestPriorOtherConditionFactory.create("c6", 2023, 5)
        val c7 = TestPriorOtherConditionFactory.create("c7", 2024, 8)

        val conditions = listOf(c1, c2, c3, c4, c5, c6, c7).sortedWith(PriorOtherConditionDescendingDateComparator())

        assertThat(conditions).containsExactly(c7, c5, c6, c1, c3, c4, c2)
    }


}