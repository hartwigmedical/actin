package com.hartwig.actin.molecular.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Characteristics {

    public abstract double purity();

    @NotNull
    public abstract String qc();

    @Nullable
    public abstract PredictedTumorOrigin predictedTumorOrigin();

    @Nullable
    public abstract Boolean isMicrosatelliteUnstable();

    @Nullable
    public abstract Boolean isHomologousRepairDeficient();

    @Nullable
    public abstract Double tumorMutationalBurden();

    @Nullable
    public abstract Integer tumorMutationalLoad();

    @Nullable
    public abstract String dpyd();
}
