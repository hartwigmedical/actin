package com.hartwig.actin.molecular.datamodel;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class MolecularRecord {

    @NotNull
    public abstract String sampleId();

    @NotNull
    public abstract ExperimentType type();

    @Nullable
    public abstract LocalDate date();

    public abstract boolean hasReliableQuality();

    @NotNull
    public abstract Set<GeneMutation> mutations();

    @NotNull
    public abstract Set<String> activatedGenes();

    @NotNull
    public abstract Set<InactivatedGene> inactivatedGenes();

    @NotNull
    public abstract Set<String> amplifiedGenes();

    @NotNull
    public abstract Set<String> wildtypeGenes();

    @NotNull
    public abstract Set<FusionGene> fusions();

    @Nullable
    public abstract Boolean isMicrosatelliteUnstable();

    @Nullable
    public abstract Boolean isHomologousRepairDeficient();

    @Nullable
    public abstract Double tumorMutationalBurden();

    @Nullable
    public abstract Integer tumorMutationalLoad();

    @NotNull
    public abstract List<MolecularEvidence> actinTreatmentEvidence();

    @NotNull
    public abstract String generalTrialSource();

    @NotNull
    public abstract List<MolecularEvidence> generalTrialEvidence();

    @NotNull
    public abstract String generalEvidenceSource();

    @NotNull
    public abstract List<MolecularEvidence> generalResponsiveEvidence();

    @NotNull
    public abstract List<MolecularEvidence> generalResistanceEvidence();

}
