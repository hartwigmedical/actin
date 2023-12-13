package com.hartwig.actin.algo.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.time.LocalDate

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class TreatmentMatch {
    abstract fun patientId(): String
    abstract fun sampleId(): String
    abstract fun referenceDate(): LocalDate
    abstract fun referenceDateIsLive(): Boolean
    abstract fun trialMatches(): List<TrialMatch?>
}
