package com.hartwig.actin.molecular.datamodel.orange.driver

import com.hartwig.actin.molecular.datamodel.TranscriptImpact

data class ExtendedVariantDetails(
    val variantCopyNumber: Double,
    val totalCopyNumber: Double,
    val isBiallelic: Boolean,
    val phaseGroups: Set<Int>?,
    val clonalLikelihood: Double,
    val otherImpacts: Set<TranscriptImpact> = emptySet()
)