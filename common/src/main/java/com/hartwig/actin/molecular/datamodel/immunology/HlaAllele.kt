package com.hartwig.actin.molecular.datamodel.immunology

data class HlaAllele(
    val name: String,
    val tumorCopyNumber: Double,
    val hasSomaticMutations: Boolean
)
