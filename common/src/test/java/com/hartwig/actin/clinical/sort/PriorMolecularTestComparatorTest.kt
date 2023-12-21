package com.hartwig.actin.clinical.sort

import com.google.common.collect.Lists
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test

class PriorMolecularTestComparatorTest {
    @Test
    fun canSortPriorMolecularTests() {
        val test1 = withItem("ZZZ")
        val test2 = withItem("TP53")
        val test3 = withItem("ZZZ")
        val test4 = withItem("CK20")
        val sorted: List<PriorMolecularTest> = Lists.newArrayList(test1, test2, test3, test4)
        sorted.sort(PriorMolecularTestComparator())
        Assert.assertEquals(test4, sorted[0])
        Assert.assertEquals(test2, sorted[1])
        Assert.assertEquals("ZZZ", sorted[2].item())
        Assert.assertEquals("ZZZ", sorted[3].item())
    }

    companion object {
        private fun withItem(item: String): PriorMolecularTest {
            return ImmutablePriorMolecularTest.builder().item(item).test(Strings.EMPTY).impliesPotentialIndeterminateStatus(false).build()
        }
    }
}