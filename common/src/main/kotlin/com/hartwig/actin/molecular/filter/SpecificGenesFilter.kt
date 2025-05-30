package com.hartwig.actin.molecular.filter

internal class SpecificGenesFilter(private val allowedGenes: Set<String>) : GeneFilter {

    override fun include(gene: String): Boolean {
        return allowedGenes.contains(gene)
    }
}
