package com.hartwig.actin.molecular.datamodel.driver;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Variant implements Driver, GeneAlteration {

    @NotNull
    public abstract VariantType type();

    public abstract double variantCopyNumber();

    public abstract double totalCopyNumber();

    public abstract boolean isBiallelic();

    public abstract boolean isHotspot();

    public abstract double clonalLikelihood();

    @NotNull
    public abstract TranscriptImpact canonicalImpact();

    @NotNull
    public abstract Set<TranscriptImpact> otherImpacts();

    //public abstract boolean isReportable();
}
