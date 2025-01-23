package com.hartwig.actin.datamodel.molecular.pharmaco

data class PharmacoEntry(
    val gene: PharmacoGene,
    val haplotypes: Set<Haplotype>
) : Comparable<PharmacoEntry> {

    override fun compareTo(other: PharmacoEntry): Int {
        return Comparator.comparing(PharmacoEntry::gene)
            .thenComparing({ it.haplotypes.size }, Int::compareTo)
            .compare(this, other)
    }
}