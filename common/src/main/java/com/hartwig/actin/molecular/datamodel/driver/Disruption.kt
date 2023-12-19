package com.hartwig.actin.molecular.datamodel.driver

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class Disruption : Driver, GeneAlteration {
    abstract fun type(): DisruptionType
    abstract fun junctionCopyNumber(): Double
    abstract fun undisruptedCopyNumber(): Double
    abstract fun regionType(): RegionType
    abstract fun codingContext(): CodingContext
    abstract fun clusterGroup(): Int
}
