package com.hartwig.actin.molecular.datamodel.pharmaco

data class PharmacoEntry(
    val gene: String,
    val haplotypes: Set<Haplotype>
) : Comparable<PharmacoEntry> {

    override fun compareTo(other: PharmacoEntry): Int {
        return Comparator.comparing(PharmacoEntry::gene)
            .thenComparing({ it.haplotypes.size }, Int::compareTo)
            .compare(this, other)
    }
}
