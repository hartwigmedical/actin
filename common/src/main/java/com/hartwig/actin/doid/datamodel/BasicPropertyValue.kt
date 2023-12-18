package com.hartwig.actin.doid.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class BasicPropertyValue {
    abstract fun pred(): String
    abstract fun `val`(): String
}
