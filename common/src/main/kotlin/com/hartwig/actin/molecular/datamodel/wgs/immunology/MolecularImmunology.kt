package com.hartwig.actin.molecular.datamodel.wgs.immunology

data class MolecularImmunology(
    val isReliable: Boolean,
    val hlaAlleles: Set<HlaAllele>
)
