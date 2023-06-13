package com.hartwig.actin.clinical.feed.digitalfile

import com.hartwig.actin.clinical.feed.FeedEntry
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.time.LocalDate

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class DigitalFileEntry : FeedEntry {
    abstract override fun subject(): String
    abstract fun authored(): LocalDate
    abstract fun description(): String
    abstract fun itemText(): String
    abstract fun itemAnswerValueValueString(): String
    val isBloodTransfusionEntry: Boolean
        get() = description() == BLOOD_TRANSFUSION_DESCRIPTION
    val isToxicityEntry: Boolean
        get() = description() == TOXICITY_DESCRIPTION

    companion object {
        private const val BLOOD_TRANSFUSION_DESCRIPTION = "Aanvraag bloedproducten_test"
        private const val TOXICITY_DESCRIPTION = "ONC Kuuroverzicht"
    }
}