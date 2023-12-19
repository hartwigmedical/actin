package com.hartwig.actin.molecular.datamodel.evidence;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ActionableEvidence {

    @NotNull
    public abstract Set<String> approvedTreatments();

    @NotNull
    public abstract Set<String> externalEligibleTrials();

    @NotNull
    public abstract Set<String> onLabelExperimentalTreatments();

    @NotNull
    public abstract Set<String> offLabelExperimentalTreatments();

    @NotNull
    public abstract Set<String> preClinicalTreatments();

    @NotNull
    public abstract Set<String> knownResistantTreatments();

    @NotNull
    public abstract Set<String> suspectResistantTreatments();
}
