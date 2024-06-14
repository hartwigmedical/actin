package com.hartwig.actin.molecular.datamodel.orange.driver

data class ExtendedFusion(
    val fusedExonUp: Int,
    val fusedExonDown: Int,
    val isAssociatedWithDrugResistance: Boolean?,
)