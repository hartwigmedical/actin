package com.hartwig.actin.clinical.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.time.LocalDate

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class Toxicity {
    abstract fun name(): String
    abstract fun categories(): Set<String?>
    abstract fun evaluatedDate(): LocalDate
    abstract fun source(): ToxicitySource
    abstract fun grade(): Int?
}