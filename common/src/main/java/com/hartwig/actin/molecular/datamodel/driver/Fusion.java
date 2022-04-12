package com.hartwig.actin.molecular.datamodel.driver;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Fusion {

    @NotNull
    public abstract String fiveGene();

    @NotNull
    public abstract String threeGene();

    @NotNull
    public abstract String details();

    @NotNull
    public abstract FusionDriverType driverType();

    @NotNull
    public abstract String driverLikelihood();

}
