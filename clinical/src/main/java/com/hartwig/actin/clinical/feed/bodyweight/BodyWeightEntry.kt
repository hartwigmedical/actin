package com.hartwig.actin.clinical.feed.bodyweight

import com.hartwig.actin.clinical.feed.FeedEntry
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.time.LocalDate

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class BodyWeightEntry : FeedEntry {
    abstract override fun subject(): String
    abstract fun valueQuantityValue(): Double
    abstract fun valueQuantityUnit(): String
    abstract fun effectiveDateTime(): LocalDate
}