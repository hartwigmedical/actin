package com.hartwig.actin.clinical.sort

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PriorMolecularTestComparatorTest {

    @Test
    fun `Should sort prior molecular tests`() {
        val test1 = withItem("ZZZ")
        val test2 = withItem("TP53")
        val test3 = withItem("ZZZ")
        val test4 = withItem("CK20")
        val sorted = listOf(test1, test2, test3, test4).sortedWith(PriorMolecularTestComparator())

        assertThat(sorted[0]).isEqualTo(test4)
        assertThat(sorted[1]).isEqualTo(test2)
        assertThat(sorted[2].item).isEqualTo("ZZZ")
        assertThat(sorted[3].item).isEqualTo("ZZZ")
    }

    private fun withItem(item: String): PriorMolecularTest {
        return PriorMolecularTest(item = item, test = "", impliesPotentialIndeterminateStatus = false)
    }
}