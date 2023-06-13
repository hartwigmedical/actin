package com.hartwig.actin.clinical.feed.lab

import com.hartwig.actin.clinical.feed.FeedEntry
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.time.LocalDate

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class LabEntry : FeedEntry {
    abstract override fun subject(): String
    abstract fun codeCodeOriginal(): String
    abstract fun codeDisplayOriginal(): String
    abstract fun valueQuantityComparator(): String
    abstract fun valueQuantityValue(): Double
    abstract fun valueQuantityUnit(): String
    abstract fun referenceRangeText(): String
    abstract fun effectiveDateTime(): LocalDate
}