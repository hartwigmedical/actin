package com.hartwig.actin.datamodel.molecular.orange.immunology

data class HlaAllele(
    val name: String,
    val tumorCopyNumber: Double?,
    val hasSomaticMutations: Boolean?
) : Comparable<HlaAllele> {

    override fun compareTo(other: HlaAllele): Int {
        return name.compareTo(other.name)
    }
}
