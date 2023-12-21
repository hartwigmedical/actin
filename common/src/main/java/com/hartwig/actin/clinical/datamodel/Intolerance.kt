package com.hartwig.actin.clinical.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class Intolerance {
    abstract fun name(): String
    abstract fun doids(): Set<String?>
    abstract fun category(): String
    abstract fun subcategories(): Set<String?>
    abstract fun type(): String
    abstract fun clinicalStatus(): String
    abstract fun verificationStatus(): String
    abstract fun criticality(): String
}
