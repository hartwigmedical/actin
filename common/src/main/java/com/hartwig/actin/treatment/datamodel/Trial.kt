package com.hartwig.actin.treatment.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class Trial {
    abstract fun identification(): TrialIdentification
    abstract fun generalEligibility(): List<Eligibility?>
    abstract fun cohorts(): List<Cohort?>
}
