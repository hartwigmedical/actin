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
    public abstract String geneStart();

    @NotNull
    public abstract String geneContextStart();

    @NotNull
    public abstract String geneEnd();

    @NotNull
    public abstract String geneContextEnd();

    @NotNull
    public abstract FusionLikelihood likelihood();

}
