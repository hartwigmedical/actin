package com.hartwig.actin.molecular.datamodel.driver;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TranscriptImpact {

    @NotNull
    public abstract String transcriptId();

    @NotNull
    public abstract String hgvsCodingImpact();

    @NotNull
    public abstract String hgvsProteinImpact();

    @Nullable
    public abstract Integer affectedCodon();

    @Nullable
    public abstract Integer affectedExon();

    public abstract boolean isSpliceRegion();

    @NotNull
    public abstract Set<VariantEffect> effects();

    @Nullable
    public abstract CodingEffect codingEffect();

}
