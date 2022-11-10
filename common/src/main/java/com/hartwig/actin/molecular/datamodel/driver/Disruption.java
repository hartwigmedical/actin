package com.hartwig.actin.molecular.datamodel.driver;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Disruption implements Driver, GeneAlteration {

    @NotNull
    public abstract String type();

    public abstract double junctionCopyNumber();

    public abstract double undisruptedCopyNumber();

    @NotNull
    public abstract RegionType regionType();

    @NotNull
    public abstract CodingContext codingContext();

    @Nullable
    public abstract Integer clusterGroup();

    @NotNull
    public abstract String range();
}
