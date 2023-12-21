package com.hartwig.actin.clinical.datamodel;

import java.time.LocalDate;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class VitalFunction {

    @NotNull
    public abstract LocalDate date();

    @NotNull
    public abstract VitalFunctionCategory category();

    @NotNull
    public abstract String subcategory();

    public abstract double value();

    @NotNull
    public abstract String unit();
}
