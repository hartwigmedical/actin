package com.hartwig.actin.doid.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class Edge {
    abstract fun subject(): String
    abstract fun subjectDoid(): String
    abstract fun `object`(): String
    abstract fun objectDoid(): String
    abstract fun predicate(): String
}
