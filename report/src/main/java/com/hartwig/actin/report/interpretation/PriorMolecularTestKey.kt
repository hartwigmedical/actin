package com.hartwig.actin.report.interpretation

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class PriorMolecularTestKey {
    abstract fun test(): String
    abstract fun scoreText(): String
}