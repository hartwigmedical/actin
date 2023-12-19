package com.hartwig.actin.molecular.datamodel.driver

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class Virus : Driver {
    abstract fun name(): String
    abstract fun type(): VirusType

    @JvmField
    abstract val isReliable: Boolean
    abstract fun integrations(): Int
}
