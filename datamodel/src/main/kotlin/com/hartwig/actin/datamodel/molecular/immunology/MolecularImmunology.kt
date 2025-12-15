package com.hartwig.actin.datamodel.molecular.immunology

data class MolecularImmunology(
    val isReliable: Boolean,
    val hlaAlleles: Set<HlaAllele>
)
