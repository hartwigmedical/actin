package com.hartwig.actin.molecular.datamodel.orange.pharmaco

const val HAPLOTYPE_SEPARATOR: String = "_"
const val HOMOZYGOUS_ZYGOSITY_STRING: String = "HOM"
const val HETEROZYGOUS_ZYGOSITY_STRING: String = "HET"
const val UNKNOWN_ALLELE_STRING: String = "Unresolved Haplotype"

data class Haplotype(
    val allele: String,
    val alleleCount: Int,
    val function: String,
) : Comparable<Haplotype> {

    override fun compareTo(other: Haplotype): Int {
        return Comparator.comparing(Haplotype::allele).thenComparing(Haplotype::alleleCount).thenComparing(Haplotype::function).compare(this, other)
    }

    fun toHaplotypeString(): String {
        return if (allele == UNKNOWN_ALLELE_STRING) {
            allele
        } else {
            allele + HAPLOTYPE_SEPARATOR + toZygosityString()
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