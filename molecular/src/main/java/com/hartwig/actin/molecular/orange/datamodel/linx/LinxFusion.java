package com.hartwig.actin.molecular.orange.datamodel.linx;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class LinxFusion {

    public abstract boolean reported();

    @NotNull
    public abstract LinxFusionType type();

    @NotNull
    public abstract String geneStart();

    @NotNull
    public abstract String geneTranscriptStart();

    public abstract int fusedExonUp();

    @NotNull
    public abstract String geneEnd();

    @NotNull
    public abstract String geneTranscriptEnd();

    public abstract int fusedExonDown();

    @Nullable
    public abstract LinxFusionDriverLikelihood driverLikelihood();

}
