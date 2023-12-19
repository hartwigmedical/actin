package com.hartwig.actin.molecular.datamodel.evidence

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class ActionableEvidence {
    abstract fun approvedTreatments(): Set<String?>
    abstract fun externalEligibleTrials(): Set<String?>
    abstract fun onLabelExperimentalTreatments(): Set<String?>
    abstract fun offLabelExperimentalTreatments(): Set<String?>
    abstract fun preClinicalTreatments(): Set<String?>
    abstract fun knownResistantTreatments(): Set<String?>
    abstract fun suspectResistantTreatments(): Set<String?>
}
