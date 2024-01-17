package com.hartwig.actin.molecular.interpretation;

import com.google.common.collect.Multimap;
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class AggregatedEvidence {

    @NotNull
    public abstract Multimap<String, String> approvedTreatmentsPerEvent();

    @NotNull
    public abstract Multimap<String, ExternalTrial> externalEligibleTrialsPerEvent();

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
