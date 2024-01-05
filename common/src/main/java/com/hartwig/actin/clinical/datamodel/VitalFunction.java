package com.hartwig.actin.clinical.datamodel;

import java.time.LocalDateTime;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class VitalFunction {

    @NotNull
    public abstract LocalDateTime date();

    @NotNull
    public abstract VitalFunctionCategory category();

    @NotNull
    public abstract String subcategory();

    public abstract double value();

    @NotNull
    public abstract String unit();

    @NotNull
    public abstract Boolean valid();
}
