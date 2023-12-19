package com.hartwig.actin.molecular.filter

import com.google.common.collect.Sets
import org.junit.Assert
import org.junit.Test

class SpecificGenesFilterTest {
    @Test
    fun canFilterGenes() {
        val filter = SpecificGenesFilter(Sets.newHashSet("gene A"))
        Assert.assertTrue(filter.include("gene A"))
        Assert.assertFalse(filter.include("gene B"))
    }
}