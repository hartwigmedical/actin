package com.hartwig.actin.clinical.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

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

    @NotNull
    AtcLevel chemicalSubstance();
}
