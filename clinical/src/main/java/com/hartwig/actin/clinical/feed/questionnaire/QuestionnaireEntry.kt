package com.hartwig.actin.clinical.feed.questionnaire

import com.hartwig.actin.clinical.feed.FeedEntry
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.time.LocalDate

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class QuestionnaireEntry : FeedEntry {
    abstract override fun subject(): String
    abstract fun authored(): LocalDate
    abstract fun description(): String
    abstract fun itemText(): String
    abstract fun text(): String
}