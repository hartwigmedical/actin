package com.hartwig.actin.molecular.datamodel.orange.driver

import com.hartwig.actin.molecular.datamodel.ProteinEffect

data class ExtendedFusion(
    val fusedExonUp: Int,
    val fusedExonDown: Int,
    val driverType: FusionDriverType,
    val proteinEffect: ProteinEffect,
    val isAssociatedWithDrugResistance: Boolean?,
)