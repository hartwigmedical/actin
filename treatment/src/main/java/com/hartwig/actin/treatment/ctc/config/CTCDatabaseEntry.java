package com.hartwig.actin.treatment.ctc.config;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class CTCDatabaseEntry {

    public abstract int studyId();

    @NotNull
    public abstract String studyMETC();

    @NotNull
    public abstract String studyAcronym();

    @NotNull
    public abstract String studyTitle();

    @NotNull
    public abstract String studyStatus();

    @Nullable
    public abstract Integer cohortId();

    @Nullable
    public abstract Integer cohortParentId();

    @Nullable
    public abstract String cohortName();

    @Nullable
    public abstract String cohortStatus();

    @Nullable
    public abstract Integer cohortSlotsNumberAvailable();

    @Nullable
    public abstract String cohortSlotsDateAvailable();
}
