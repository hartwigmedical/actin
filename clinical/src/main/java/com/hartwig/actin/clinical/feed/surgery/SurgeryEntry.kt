package com.hartwig.actin.clinical.feed.surgery

import com.hartwig.actin.clinical.feed.FeedEntry
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.time.LocalDate

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class SurgeryEntry : FeedEntry {
    abstract override fun subject(): String
    abstract fun classDisplay(): String
    abstract fun periodStart(): LocalDate
    abstract fun periodEnd(): LocalDate
    abstract fun codeCodingDisplayOriginal(): String
    abstract fun encounterStatus(): String
    abstract fun procedureStatus(): String
}