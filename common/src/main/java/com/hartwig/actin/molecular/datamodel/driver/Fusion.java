package com.hartwig.actin.molecular.datamodel.driver;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Fusion implements Driver {

    @NotNull
    public abstract String geneStart();

    @NotNull
    public abstract String geneTranscriptStart();

    @NotNull
    public abstract String geneContextStart();

    public abstract int fusedExonUp();

    @NotNull
    public abstract String geneEnd();

    @NotNull
    public abstract String geneTranscriptEnd();

    @NotNull
    public abstract String geneContextEnd();

    public abstract int fusedExonDown();

    @NotNull
    public abstract FusionDriverType driverType();

    @NotNull
    public abstract ProteinEffect proteinEffect();

    @Nullable
    public abstract Boolean isAssociatedWithDrugResistance();

}
