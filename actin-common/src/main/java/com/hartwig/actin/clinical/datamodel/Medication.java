package com.hartwig.actin.clinical.datamodel;

import java.time.LocalDate;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Medication {

    @NotNull
    public abstract String name();

    @NotNull
    public abstract String type();

    public abstract double dosage();

    @NotNull
    public abstract String unit();

    @NotNull
    public abstract String frequencyUnit();

    @Nullable
    public abstract Boolean ifNeeded();

    @Nullable
    public abstract LocalDate startDate();

    @Nullable
    public abstract LocalDate stopDate();

}
