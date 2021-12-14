package com.hartwig.actin.molecular.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class FusionGene {

    @NotNull
    public abstract String fiveGene();

    @NotNull
    public abstract String threeGene();

}