package com.hartwig.actin.clinical.datamodel;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class AtcClassification {

    @NotNull
    public abstract AtcLevel anatomicalMainGroup();

    @NotNull
    public abstract AtcLevel therapeuticSubGroup();

    @NotNull
    public abstract AtcLevel pharmacologicalSubGroup();

    @NotNull
    public abstract AtcLevel chemicalSubGroup();

    @Nullable
    public abstract AtcLevel chemicalSubstance();

    @NotNull
    public Set<AtcLevel> allLevels() {
        return Stream.concat(Optional.ofNullable(chemicalSubstance()).stream(),
                        Stream.of(anatomicalMainGroup(), therapeuticSubGroup(), pharmacologicalSubGroup(), chemicalSubGroup()))
                .collect(Collectors.toSet());
    }
}
