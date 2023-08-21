package com.hartwig.actin.report.interpretation

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class EvaluatedCohort {
    abstract fun trialId(): String
    abstract fun acronym(): String
    abstract fun cohort(): String?
    abstract fun molecularEvents(): Set<String?>
    abstract val isPotentiallyEligible: Boolean
    abstract val isOpen: Boolean
    abstract fun hasSlotsAvailable(): Boolean
    abstract fun warnings(): Set<String?>
    abstract fun fails(): Set<String?>
}