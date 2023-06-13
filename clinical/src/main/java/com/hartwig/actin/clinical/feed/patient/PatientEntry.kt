package com.hartwig.actin.clinical.feed.patient

import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.feed.FeedEntry
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.time.LocalDate

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class PatientEntry : FeedEntry {
    abstract override fun subject(): String
    abstract fun birthYear(): Int
    abstract fun gender(): Gender
    abstract fun periodStart(): LocalDate
    abstract fun periodEnd(): LocalDate?
}