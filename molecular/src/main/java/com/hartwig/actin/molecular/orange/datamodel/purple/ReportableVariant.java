package com.hartwig.actin.molecular.orange.datamodel.purple;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ReportableVariant {

    @NotNull
    public abstract String gene();

    @NotNull
    public abstract String hgvsProteinImpact();

    @NotNull
    public abstract String hgvsCodingImpact();

    @NotNull
    public abstract String effect();

    public abstract double alleleCopyNumber();

    public abstract double totalCopyNumber();

    @NotNull
    public abstract VariantHotspot hotspot();

    public abstract boolean biallelic();

    public abstract double driverLikelihood();

    public abstract double clonalLikelihood();
}
