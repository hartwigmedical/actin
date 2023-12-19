package com.hartwig.actin.molecular.interpretation

import com.google.common.collect.Multimap
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class AggregatedEvidence {
    abstract fun approvedTreatmentsPerEvent(): Multimap<String?, String?>
    abstract fun externalEligibleTrialsPerEvent(): Multimap<String?, String?>
    abstract fun onLabelExperimentalTreatmentsPerEvent(): Multimap<String?, String?>
    abstract fun offLabelExperimentalTreatmentsPerEvent(): Multimap<String?, String?>
    abstract fun preClinicalTreatmentsPerEvent(): Multimap<String?, String?>
    abstract fun knownResistantTreatmentsPerEvent(): Multimap<String?, String?>
    abstract fun suspectResistanceTreatmentsPerEvent(): Multimap<String?, String?>
}
