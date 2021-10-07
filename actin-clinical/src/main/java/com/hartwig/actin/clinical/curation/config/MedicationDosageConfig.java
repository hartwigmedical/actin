package com.hartwig.actin.clinical.curation.config;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class MedicationDosageConfig implements CurationConfig {

    @NotNull
    @Override
    public abstract String input();

    @Nullable
    public abstract Double dosageMin();

    @Nullable
    public abstract Double dosageMax();

    @NotNull
    public abstract String unit();

    @NotNull
    public abstract String frequencyUnit();

    @Nullable
    public abstract Boolean ifNeeded();
}
