package com.hartwig.actin.molecular.filter

object TestGeneFilterFactory {

    fun createAlwaysValid(): GeneFilter {
        return AlwaysValidFilter()
    }

    fun createNeverValid(): GeneFilter {
        return SpecificGenesFilter(emptySet())
    }

    @JvmStatic
    fun createValidForGenes(vararg genes: String): GeneFilter {
        return SpecificGenesFilter(setOf(*genes))
    }
}
