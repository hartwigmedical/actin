package com.hartwig.actin.datamodel.molecular.immunology

import com.hartwig.actin.datamodel.molecular.driver.HlaAllele

data class MolecularImmunology(
    val isReliable: Boolean,
    val hlaAlleles: Set<HlaAllele>
)
