package com.hartwig.actin.molecular.datamodel.orange.driver

data class ExtendedVariantDetails(
    val variantCopyNumber: Double,
    val totalCopyNumber: Double,
    val isBiallelic: Boolean,
    val phaseGroups: Set<Int>?,
    val clonalLikelihood: Double,
)