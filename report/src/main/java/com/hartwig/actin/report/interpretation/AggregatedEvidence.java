package com.hartwig.actin.report.interpretation;

import com.google.common.collect.Multimap;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class AggregatedEvidence {

    @NotNull
    public abstract Multimap<String, String> approvedTreatmentsPerEvent();

    @NotNull
    public abstract Multimap<String, String> externalEligibleTrialsPerEvent();

    @NotNull
    public abstract Multimap<String, String> onLabelExperimentalTreatmentsPerEvent();

    @NotNull
    public abstract Multimap<String, String> offLabelExperimentalTreatmentsPerEvent();

    @NotNull
    public abstract Multimap<String, String> preClinicalTreatmentsPerEvent();

    @NotNull
    public abstract Multimap<String, String> knownResistantTreatmentsPerEvent();

    @NotNull
    public abstract Multimap<String, String> suspectResistanceTreatmentsPerEvent();

}
