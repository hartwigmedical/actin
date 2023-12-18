package com.hartwig.actin.doid.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class Node {
    abstract fun doid(): String
    abstract fun url(): String
    abstract fun term(): String?
    abstract fun type(): String?
    abstract fun metadata(): Metadata?

    @Value.Derived
    fun snomedConceptId(): String? {
        return if (metadata() != null) metadata()!!.snomedConceptId() else null
    }
}
