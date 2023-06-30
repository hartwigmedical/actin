package com.hartwig.actin.clinical.datamodel.treatment;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class CombinedTherapy implements Therapy {

    @NotNull
    public abstract Set<Therapy> therapies();

    @Override
    @NotNull
    public Set<TreatmentCategory> categories() {
        return extractFromComponents(Therapy::categories);
    }

    @Override
    @NotNull
    public Set<Drug> drugs() {
        return extractFromComponents(Therapy::drugs);
    }

    private <T> Set<T> extractFromComponents(Function<Therapy, Collection<T>> extract) {
        return therapies().stream().map(extract).flatMap(Collection::stream).collect(Collectors.toSet());
    }
}
