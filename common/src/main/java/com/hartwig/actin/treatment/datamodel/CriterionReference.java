package com.hartwig.actin.treatment.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class CriterionReference {

    @NotNull
    public abstract String id();

    @NotNull
    public abstract String text();
}
