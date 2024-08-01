package com.hartwig.actin.molecular.datamodel.orange.pharmaco

private const val HAPLOTYPE_SEPARATOR: String = "_"
private const val HOMOZYGOUS_ZYGOSITY_STRING: String = "HOM"
private const val HETEROZYGOUS_ZYGOSITY_STRING: String = "HET"
private const val UNKNOWN_ALLELE_STRING: String = "Unresolved Haplotype"

data class Haplotype(
    val name: String,
    val function: String,
) : Comparable<Haplotype> {
    val allele = name.substringBefore("_")
    val alleleCount = if ("HOM" in name) 2 else 1
    
    override fun compareTo(other: Haplotype): Int {
        return Comparator.comparing(Haplotype::allele).thenComparing(Haplotype::alleleCount).thenComparing(Haplotype::function).compare(this, other)
    }

    constructor(allele: String, alleleCount: Int, function: String) : this(
        name = allele + HAPLOTYPE_SEPARATOR + toZygosityString(alleleCount),
        function = function
    )
    
    fun toHaplotypeString(): String {
        return if (allele == UNKNOWN_ALLELE_STRING) {
            allele
        } else {
            allele + HAPLOTYPE_SEPARATOR + toZygosityString(alleleCount)
        }
    }
}

private fun toZygosityString(count: Int): String {
    return when (count) {
        1 -> HETEROZYGOUS_ZYGOSITY_STRING
        2 -> HOMOZYGOUS_ZYGOSITY_STRING
        else -> throw IllegalArgumentException(String.format("Could not convert allele count %s to a zygosity", count))
    }
}
