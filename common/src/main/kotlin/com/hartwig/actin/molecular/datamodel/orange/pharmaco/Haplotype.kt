package com.hartwig.actin.molecular.datamodel.orange.pharmaco

private const val HAPLOTYPE_SEPARATOR: String = "_"
private const val HOMOZYGOUS_ZYGOSITY_STRING: String = "HOM"
private const val HETEROZYGOUS_ZYGOSITY_STRING: String = "HET"
private const val UNKNOWN_ALLELE_STRING: String = "Unresolved Haplotype"

data class Haplotype(
    val allele: String,
    val alleleCount: Int,
    val function: HaplotypeFunction,
) : Comparable<Haplotype> {

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
            allele + HAPLOTYPE_SEPARATOR + toZygosityString()
        }
    }
}

    private fun toZygosityString(): String {
        return when (alleleCount) {
            1 -> HETEROZYGOUS_ZYGOSITY_STRING
            2 -> HOMOZYGOUS_ZYGOSITY_STRING
            else -> throw IllegalArgumentException(String.format("Could not convert allele count %s to a zygosity", alleleCount))
        }
    }
}
