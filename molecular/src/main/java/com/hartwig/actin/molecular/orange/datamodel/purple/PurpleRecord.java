package com.hartwig.actin.molecular.orange.datamodel.purple;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class PurpleRecord {

    public abstract boolean containsTumorCells();

    public abstract double purity();

    public abstract boolean hasSufficientQuality();

    @NotNull
    public abstract String microsatelliteStabilityStatus();

    public abstract double tumorMutationalBurden();

    public abstract int tumorMutationalLoad();

    @NotNull
    public abstract Set<PurpleVariant> variants();

    @NotNull
    public abstract Set<PurpleGainLoss> gainsLosses();
}
