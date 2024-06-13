package com.hartwig.actin.molecular.datamodel.orange.driver

import com.hartwig.actin.molecular.datamodel.TranscriptImpact
import com.hartwig.actin.molecular.datamodel.VariantType

data class ExtendedVariant(
    val variantCopyNumber: Double,
    val totalCopyNumber: Double,
    val isBiallelic: Boolean,
    val phaseGroups: Set<Int>?,
    val otherImpacts: Set<TranscriptImpact>,
    val chromosome: String,
    val position: Int,
    val ref: String,
    val alt: String,
    val type: VariantType,
    val isHotspot: Boolean,
    val clonalLikelihood: Double,
    val canonicalImpact: TranscriptImpact,
)