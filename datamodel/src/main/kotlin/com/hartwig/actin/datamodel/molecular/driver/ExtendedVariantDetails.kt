package com.hartwig.actin.datamodel.molecular.driver

data class ExtendedVariantDetails(
    val variantCopyNumber: Double,
    val totalCopyNumber: Double,
    val isBiallelic: Boolean,
    val phaseGroups: Set<Int>?,
    val clonalLikelihood: Double
)