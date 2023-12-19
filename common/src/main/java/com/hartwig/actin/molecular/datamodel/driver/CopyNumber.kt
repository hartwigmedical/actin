package com.hartwig.actin.molecular.datamodel.driver

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class CopyNumber : Driver, GeneAlteration {
    abstract fun type(): CopyNumberType
    abstract fun minCopies(): Int
    abstract fun maxCopies(): Int
}
