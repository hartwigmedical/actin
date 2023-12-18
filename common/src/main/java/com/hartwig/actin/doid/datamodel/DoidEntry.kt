package com.hartwig.actin.doid.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class DoidEntry {
    abstract fun id(): String
    abstract fun nodes(): List<Node?>
    abstract fun edges(): List<Edge?>
    abstract fun metadata(): GraphMetadata
    abstract fun logicalDefinitionAxioms(): List<LogicalDefinitionAxioms?>?
    abstract fun equivalentNodesSets(): List<String?>?
    abstract fun domainRangeAxioms(): List<String?>?
    abstract fun propertyChainAxioms(): List<String?>?
}
