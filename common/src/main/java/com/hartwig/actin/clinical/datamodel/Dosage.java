package com.hartwig.actin.clinical.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Dosage {

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
    public abstract Double periodBetweenValue();

    @Nullable
    public abstract String periodBetweenUnit();

    @Nullable
    public abstract Boolean ifNeeded();
}
