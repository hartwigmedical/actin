package com.hartwig.actin.clinical.sort

import com.hartwig.actin.clinical.datamodel.PriorIHCTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PriorIHCTestComparatorTest {

    @Test
    fun `Should sort prior molecular tests`() {
        val test1 = withItem("ZZZ")
        val test2 = withItem("TP53")
        val test3 = withItem("ZZZ")
        val test4 = withItem("CK20")
        val test5 = withItem(null)
        val sorted = listOf(test1, test2, test3, test4, test5).sortedWith(PriorIHCTestComparator())

        assertThat(sorted[0]).isEqualTo(test5)
        assertThat(sorted[1]).isEqualTo(test4)
        assertThat(sorted[2]).isEqualTo(test2)
        assertThat(sorted[3].item).isEqualTo("ZZZ")
        assertThat(sorted[4].item).isEqualTo("ZZZ")
    }

    private fun withItem(item: String?): PriorIHCTest {
        return PriorIHCTest(item = item, impliesPotentialIndeterminateStatus = false)
    }
}