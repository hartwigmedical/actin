package com.hartwig.actin.datamodel.molecular.orange.immunology

data class MolecularImmunology(
    val isReliable: Boolean,
    val hlaAlleles: Set<HlaAllele>
)
