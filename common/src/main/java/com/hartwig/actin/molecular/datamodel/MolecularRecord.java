package com.hartwig.actin.molecular.datamodel;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Multimap;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class MolecularRecord {

    @NotNull
    public abstract String sampleId();

    @NotNull
    public abstract Set<String> doids();

    @NotNull
    public abstract ExperimentType type();

    @Nullable
    public abstract LocalDate date();

    public abstract boolean hasReliableQuality();

    @NotNull
    public abstract List<GeneMutation> mutations();

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

    public abstract double tumorMutationalBurden();

    public abstract int tumorMutationalLoad();

    @NotNull
    public abstract Multimap<String, String> actinTrialEligibility();

    @NotNull
    public abstract Multimap<String, String> generalTrialEligibility();

    @NotNull
    public abstract Multimap<String, String> generalResponsiveEvidence();

    @NotNull
    public abstract Multimap<String, String> generalResistanceEvidence();

}
