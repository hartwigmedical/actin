package com.hartwig.actin.treatment.input.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TreatmentInputWithName {

    @NotNull
    public abstract TreatmentCategoryInput treatment();

    @Nullable
    public abstract String name();
}
