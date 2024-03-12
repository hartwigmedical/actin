package com.hartwig.actin.molecular.filter

interface GeneFilter {

    fun include(gene: String): Boolean
}
