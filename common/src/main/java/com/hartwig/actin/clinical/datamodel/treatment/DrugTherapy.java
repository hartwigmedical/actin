package com.hartwig.actin.clinical.datamodel.treatment;

import java.util.Set;
import java.util.stream.Collectors;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class DrugTherapy implements Therapy {

    @Override
    @NotNull
    public Set<TreatmentCategory> categories() {
        return drugs().stream().map(Drug::category).collect(Collectors.toSet());
    }
}
