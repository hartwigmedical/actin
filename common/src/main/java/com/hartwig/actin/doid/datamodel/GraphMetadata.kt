package com.hartwig.actin.doid.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class GraphMetadata {
    abstract fun subsets(): List<String?>?
    abstract fun xrefs(): List<Xref?>?
    abstract fun basicPropertyValues(): List<BasicPropertyValue?>?
    abstract fun version(): String?
}
