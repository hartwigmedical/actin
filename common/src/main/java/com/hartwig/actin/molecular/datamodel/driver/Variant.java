package com.hartwig.actin.molecular.datamodel.driver;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Variant {

    @NotNull
    public abstract String gene();

    @NotNull
    public abstract String impact();

    public abstract double variantCopyNumber();

    public abstract double totalCopyNumber();

    @NotNull
    public abstract VariantDriverType driverType();

    public abstract double driverLikelihood();

    public abstract double subclonalLikelihood();
}
