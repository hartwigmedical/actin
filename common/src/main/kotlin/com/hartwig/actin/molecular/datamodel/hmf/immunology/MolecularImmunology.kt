package com.hartwig.actin.molecular.datamodel.hmf.immunology

data class MolecularImmunology(
    val isReliable: Boolean,
    val hlaAlleles: Set<HlaAllele>
)
