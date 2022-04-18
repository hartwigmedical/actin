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
    public abstract Set<EvidenceEntry> actinTrials();

    @NotNull
    public abstract String externalTrialSource();

    @NotNull
    public abstract Set<EvidenceEntry> externalTrials();

    @NotNull
    public abstract String evidenceSource();

    @NotNull
    public abstract Set<EvidenceEntry> approvedEvidence();

    @NotNull
    public abstract Set<EvidenceEntry> onLabelExperimentalEvidence();

    @NotNull
    public abstract Set<EvidenceEntry> offLabelExperimentalEvidence();

    @NotNull
    public abstract Set<EvidenceEntry> preClinicalEvidence();

    @NotNull
    public abstract Set<EvidenceEntry> knownResistanceEvidence();

    @NotNull
    public abstract Set<EvidenceEntry> suspectResistanceEvidence();
}
