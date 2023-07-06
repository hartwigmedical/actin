package com.hartwig.actin.clinical.datamodel.treatment;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Radiotherapy implements Therapy {

    @Override
    @NotNull
    @Value.Default
    public Set<Drug> drugs() {
        return Collections.emptySet();
    }

    @Override
    @NotNull
    public Set<TreatmentCategory> categories() {
        return Stream.concat(Stream.of(TreatmentCategory.RADIOTHERAPY), drugs().stream().map(Drug::category)).collect(Collectors.toSet());
    }

    @Nullable
    public abstract String radioType();

    @Nullable
    public abstract Boolean isInternal();
}
