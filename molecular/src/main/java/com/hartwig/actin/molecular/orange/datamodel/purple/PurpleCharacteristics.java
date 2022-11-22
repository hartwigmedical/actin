package com.hartwig.actin.molecular.orange.datamodel.purple;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class PurpleCharacteristics {

    @NotNull
    public abstract String microsatelliteStabilityStatus();

    public abstract double tumorMutationalBurden();

    @NotNull
    public abstract String tumorMutationalBurdenStatus();

    public abstract int tumorMutationalLoad();

    @NotNull
    public abstract String tumorMutationalLoadStatus();
}
