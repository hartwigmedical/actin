package com.hartwig.actin.molecular.orange.datamodel.purple;

import java.util.List;
import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class PurpleVariant {

    public abstract boolean reported();

    @NotNull
    public abstract PurpleVariantType type();

    @NotNull
    public abstract String gene();

    @NotNull
    public abstract String chromosome();

    public abstract int position();

    @NotNull
    public abstract String ref();

    @NotNull
    public abstract String alt();

    public abstract double adjustedCopyNumber();

    public abstract double variantCopyNumber();

    @NotNull
    public abstract PurpleHotspotType hotspot();

    public abstract double subclonalLikelihood();

    public abstract boolean biallelic();

    @Nullable
    public abstract List<Integer> localPhaseSets();

    @NotNull
    public abstract PurpleTranscriptImpact canonicalImpact();

    @NotNull
    public abstract Set<PurpleTranscriptImpact> otherImpacts();
}
