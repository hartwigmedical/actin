package com.hartwig.actin.clinical.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class PriorMolecularTest {
    abstract fun test(): String
    abstract fun item(): String
    abstract fun measure(): String?
    abstract fun scoreText(): String?
    abstract fun scoreValuePrefix(): String?
    abstract fun scoreValue(): Double?
    abstract fun scoreValueUnit(): String?
    abstract fun impliesPotentialIndeterminateStatus(): Boolean
}
