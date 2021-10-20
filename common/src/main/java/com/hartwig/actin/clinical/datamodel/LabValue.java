package com.hartwig.actin.clinical.datamodel;

import java.time.LocalDate;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class LabValue {

    @NotNull
    public abstract LocalDate date();

    @NotNull
    public abstract String code();

    @NotNull
    public abstract String name();

    @NotNull
    public abstract String comparator();

    public abstract double value();

    @NotNull
    public abstract String unit();

    @Nullable
    public abstract Double refLimitLow();

    @Nullable
    public abstract Double refLimitUp();

    @Nullable
    public abstract Boolean isOutsideRef();

}
