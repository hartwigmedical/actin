package com.hartwig.actin.molecular.datamodel.driver;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TranscriptImpact {

    @NotNull
    public abstract String transcriptId();

    public abstract boolean isCanonical();

    @NotNull
    public abstract String effect();

    @Nullable
    public abstract Integer affectedCodon();

    @Nullable
    public abstract Integer affectedExon();

    @NotNull
    public abstract String codingImpact();

    @NotNull
    public abstract String proteinImpact();

}
