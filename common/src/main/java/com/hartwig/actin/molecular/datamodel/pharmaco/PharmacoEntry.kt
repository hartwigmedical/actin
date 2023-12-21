package com.hartwig.actin.molecular.datamodel.pharmaco

data class PharmacoEntry(
    val gene: String,
    val haplotypes: Set<Haplotype>
)
