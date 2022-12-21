package com.hartwig.actin.molecular.orange.datamodel.purple;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class PurpleCharacteristics {

    @NotNull
    public abstract PurpleMicrosatelliteStatus microsatelliteStatus();

    public abstract double tumorMutationalBurdenPerMb();

    @NotNull
    public abstract PurpleTumorMutationalStatus tumorMutationalBurdenStatus();

    public abstract int tumorMutationalLoad();

    @NotNull
    public abstract PurpleTumorMutationalStatus tumorMutationalLoadStatus();
}
