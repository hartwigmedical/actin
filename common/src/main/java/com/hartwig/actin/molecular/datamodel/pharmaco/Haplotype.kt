package com.hartwig.actin.molecular.datamodel.pharmaco

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class Haplotype {
    abstract fun name(): String
    abstract fun function(): String
}
