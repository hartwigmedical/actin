package com.hartwig.actin.datamodel.clinical;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Allergy {

    @NotNull
    public abstract String name();

    @NotNull
    public abstract String category();

    @NotNull
    public abstract String criticality();

}