package com.hartwig.actin.clinical.datamodel.treatment;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Radiotherapy implements Therapy {
    public final TreatmentClass treatmentClass = TreatmentClass.RADIOTHERAPY;

    @Override
    @NotNull
    @Value.Default
    public Set<Drug> drugs() {
        return Collections.emptySet();
    }

    @Override
    @NotNull
    public Set<TreatmentType> types() {
        return (radioType() == null)
                ? drugs().stream().flatMap(drug -> drug.drugTypes().stream()).collect(Collectors.toSet())
                : addElementToSet(radioType(), drug -> drug.drugTypes().stream().map(t -> (TreatmentType) t));
    }

    @Override
    @NotNull
    public Set<TreatmentCategory> categories() {
        return addElementToSet(TreatmentCategory.RADIOTHERAPY, drug -> Stream.of(drug.category()));
    }

    @NotNull
    private <T> Set<T> addElementToSet(@NotNull T element, @NotNull Function<Drug, Stream<T>> extract) {
        return Stream.concat(Stream.of(element), drugs().stream().flatMap(extract)).collect(Collectors.toSet());
    }

    @Nullable
    public abstract RadiotherapyType radioType();

    @Nullable
    public abstract Boolean isInternal();
}
