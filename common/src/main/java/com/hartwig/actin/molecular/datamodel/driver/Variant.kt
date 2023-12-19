package com.hartwig.actin.molecular.datamodel.driver

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class Variant : Driver, GeneAlteration {
    abstract fun type(): VariantType
    abstract fun variantCopyNumber(): Double
    abstract fun totalCopyNumber(): Double

    @JvmField
    abstract val isBiallelic: Boolean

    @JvmField
    abstract val isHotspot: Boolean
    abstract fun clonalLikelihood(): Double
    abstract fun phaseGroups(): Set<Int?>?
    abstract fun canonicalImpact(): TranscriptImpact
    abstract fun otherImpacts(): Set<TranscriptImpact?>
}
