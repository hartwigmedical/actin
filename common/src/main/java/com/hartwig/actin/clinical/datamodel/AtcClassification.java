package com.hartwig.actin.clinical.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
public interface AtcClassification {

    @NotNull
    AtcLevel anatomicalMainGroup();

    @NotNull
    AtcLevel therapeuticSubGroup();

    @NotNull
    AtcLevel pharmacologicalSubGroup();

    @NotNull
    AtcLevel chemicalSubGroup();

    @Nullable
    AtcLevel chemicalSubstance();
}
