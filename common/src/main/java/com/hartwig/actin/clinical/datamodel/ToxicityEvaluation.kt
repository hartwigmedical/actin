package com.hartwig.actin.clinical.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.time.LocalDate

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class ToxicityEvaluation {
    abstract fun toxicities(): Set<ObservedToxicity?>
    abstract fun evaluatedDate(): LocalDate
    abstract fun source(): ToxicitySource
}
