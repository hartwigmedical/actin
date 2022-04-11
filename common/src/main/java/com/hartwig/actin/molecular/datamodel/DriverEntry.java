package com.hartwig.actin.molecular.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class DriverEntry {

    @NotNull
    public abstract DriverType type();

    @NotNull
    public abstract String name();

    @NotNull
    public abstract String details();

    @Nullable
    public abstract Double driverLikelihood();

    public abstract boolean actionableInActinSource();

    public abstract boolean actionableInExternalSource();

    @Nullable
    public abstract String highestResponsiveEvidenceLevel();

    @Nullable
    public abstract String highestResistanceEvidenceLevel();

}
