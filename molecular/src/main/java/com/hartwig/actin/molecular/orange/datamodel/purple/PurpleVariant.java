package com.hartwig.actin.molecular.orange.datamodel.purple;

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

    @NotNull
    public abstract String canonicalTranscript();

    @NotNull
    public abstract Set<PurpleVariantEffect> canonicalEffects();

    @NotNull
    public abstract PurpleCodingEffect canonicalCodingEffect();

    @NotNull
    public abstract String canonicalHgvsProteinImpact();

    @NotNull
    public abstract String canonicalHgvsCodingImpact();

    public abstract double totalCopyNumber();

    public abstract double alleleCopyNumber();

    @NotNull
    public abstract VariantHotspot hotspot();

    public abstract double clonalLikelihood();

    public abstract double driverLikelihood();

    public abstract boolean biallelic();

    @Nullable
    public abstract Integer localPhaseSet();
}
