package com.hartwig.actin.clinical.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class CypInteraction {
    enum class Type {
        INDUCER,
        INHIBITOR,
        SUBSTRATE
    }

    enum class Strength {
        STRONG,
        MODERATE,
        WEAK,
        SENSITIVE,
        MODERATE_SENSITIVE
    }

    abstract fun type(): Type
    abstract fun strength(): Strength
    abstract fun cyp(): String
}
