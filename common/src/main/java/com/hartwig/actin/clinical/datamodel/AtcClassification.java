package com.hartwig.actin.clinical.datamodel;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    default Set<AtcLevel> allLevels() {
        return Stream.concat(Optional.ofNullable(chemicalSubstance()).stream(),
                        Stream.of(anatomicalMainGroup(), therapeuticSubGroup(), pharmacologicalSubGroup(), chemicalSubGroup()))
                .collect(Collectors.toSet());
    }
}
