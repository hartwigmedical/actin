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

    public abstract double value();

    @NotNull
    public abstract String unit();

    public abstract double refLimitLow();

    public abstract double refLimitUp();

    public abstract boolean isOutsideRef();

    public abstract double alertLimitLow();

    public abstract double alertLimitUp();

    public abstract boolean isWithinAlert();
}
