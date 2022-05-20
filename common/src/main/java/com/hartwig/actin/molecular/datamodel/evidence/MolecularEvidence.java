package com.hartwig.actin.molecular.datamodel.evidence;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class MolecularEvidence {

    @NotNull
    public abstract String actinSource();

    @NotNull
    public abstract Set<ActinTrialEvidence> actinTrials();

    @NotNull
    public abstract String externalTrialSource();

    @NotNull
    public abstract Set<TreatmentEvidence> externalTrials();

    @NotNull
    public abstract String evidenceSource();

    @NotNull
    public abstract Set<TreatmentEvidence> approvedEvidence();

    @NotNull
    public abstract Set<TreatmentEvidence> onLabelExperimentalEvidence();

    @NotNull
    public abstract Set<TreatmentEvidence> offLabelExperimentalEvidence();

    @NotNull
    public abstract Set<TreatmentEvidence> preClinicalEvidence();

    @NotNull
    public abstract Set<TreatmentEvidence> knownResistanceEvidence();

    @NotNull
    public abstract Set<TreatmentEvidence> suspectResistanceEvidence();
}
