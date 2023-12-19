package com.hartwig.actin.molecular.filter

import com.google.common.collect.Sets

object TestGeneFilterFactory {
    @JvmStatic
    fun createAlwaysValid(): GeneFilter {
        return AlwaysValidFilter()
    }

    fun createNeverValid(): GeneFilter {
        return SpecificGenesFilter(Sets.newHashSet())
    }

    @JvmStatic
    fun createValidForGenes(vararg genes: String): GeneFilter {
        return SpecificGenesFilter(Sets.newHashSet(*genes))
    }
}
