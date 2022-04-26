package com.hartwig.actin.clinical.datamodel;

import java.time.LocalDate;
import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Medication {

    @NotNull
    public abstract String name();

    @NotNull
    public abstract Set<String> categories();

    @NotNull
    public abstract String status();

    @Nullable
    public abstract Double dosageMin();

    @Nullable
    public abstract Double dosageMax();

    @Nullable
    public abstract String dosageUnit();

    @Nullable
    public abstract Double frequency();

    @Nullable
    public abstract String frequencyUnit();

    @Nullable
    public abstract Boolean ifNeeded();

    @Nullable
    public abstract LocalDate startDate();

    @Nullable
    public abstract LocalDate stopDate();

    @Nullable
    public abstract Boolean active();

}
