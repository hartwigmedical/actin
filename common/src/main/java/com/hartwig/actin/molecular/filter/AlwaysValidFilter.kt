package com.hartwig.actin.molecular.filter

internal class AlwaysValidFilter : GeneFilter {
    override fun include(gene: String): Boolean {
        return true
    }
}
