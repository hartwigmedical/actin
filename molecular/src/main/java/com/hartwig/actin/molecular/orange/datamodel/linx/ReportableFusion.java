package com.hartwig.actin.molecular.orange.datamodel.linx;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ReportableFusion {

    @NotNull
    public abstract FusionType type();

    @NotNull
    public abstract String fiveGene();

    @NotNull
    public abstract String fiveContextStart();

    @NotNull
    public abstract String threeGene();

    @NotNull
    public abstract String threeContextEnd();

    @NotNull
    public abstract FusionLikelihood likelihood();

}
