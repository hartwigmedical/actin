package com.hartwig.actin.molecular.datamodel.wgs.pharmaco

data class Haplotype(
    val name: String,
    val function: String
) : Comparable<Haplotype> {

    override fun compareTo(other: Haplotype): Int {
        return Comparator.comparing(Haplotype::name).thenComparing(Haplotype::function).compare(this, other)
    }
}
