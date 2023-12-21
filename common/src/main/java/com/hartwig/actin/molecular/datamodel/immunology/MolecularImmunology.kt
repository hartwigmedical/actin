package com.hartwig.actin.molecular.datamodel.immunology

data class MolecularImmunology(
    val isReliable: Boolean,
    val hlaAlleles: Set<HlaAllele>
)
