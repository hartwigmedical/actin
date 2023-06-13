package com.hartwig.actin.clinical.feed.intolerance

import com.hartwig.actin.clinical.feed.FeedEntry
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.time.LocalDate

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class IntoleranceEntry : FeedEntry {
    abstract override fun subject(): String
    abstract fun assertedDate(): LocalDate
    abstract fun category(): String
    abstract fun categoryAllergyCategoryDisplay(): String
    abstract fun clinicalStatus(): String
    abstract fun verificationStatus(): String
    abstract fun codeText(): String
    abstract fun criticality(): String

    @JvmField
    abstract val isSideEffect: String
}