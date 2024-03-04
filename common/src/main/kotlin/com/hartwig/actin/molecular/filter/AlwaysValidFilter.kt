package com.hartwig.actin.molecular.filter

class AlwaysValidFilter : GeneFilter {

    override fun include(gene: String): Boolean {
        return true
    }
}
