package com.hartwig.actin.molecular.datamodel;

import java.time.LocalDate;
import java.util.List;

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

    @NotNull
    public abstract Characteristics characteristics();

    @NotNull
    public abstract List<DriverEntry> drivers();

    @NotNull
    public abstract ActinEvents events();

    @NotNull
    public abstract EvidenceAnalysis evidence();

}
