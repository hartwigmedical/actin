package com.hartwig.actin.treatment.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class CohortMetadata {
    abstract fun cohortId(): String
    abstract fun evaluable(): Boolean
    abstract fun open(): Boolean
    abstract fun slotsAvailable(): Boolean
    abstract fun blacklist(): Boolean
    abstract fun description(): String
}
