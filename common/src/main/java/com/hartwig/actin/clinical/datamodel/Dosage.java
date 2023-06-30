package com.hartwig.actin.clinical.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
public interface Dosage {

    @Nullable
    Double dosageMin();

    @Nullable
    Double dosageMax();

    @Nullable
    Double dosageValue();

    @Nullable
    String dosageUnit();

    @Nullable
    Double frequency();

    @Nullable
    String frequencyUnit();

    @Nullable
    Double periodBetweenValue();

    @Nullable
    String periodBetweenUnit();

    @Nullable
    Boolean ifNeeded();
}
