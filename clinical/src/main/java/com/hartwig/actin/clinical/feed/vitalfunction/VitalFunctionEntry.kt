package com.hartwig.actin.clinical.feed.vitalfunction

import com.hartwig.actin.clinical.feed.FeedEntry
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.time.LocalDate

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class VitalFunctionEntry : FeedEntry {
    abstract override fun subject(): String
    abstract fun effectiveDateTime(): LocalDate
    abstract fun codeDisplayOriginal(): String
    abstract fun componentCodeDisplay(): String
    abstract fun quantityUnit(): String
    abstract fun quantityValue(): Double
}