package com.hartwig.actin.molecular.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class GeneMutation {

    @NotNull
    public abstract String gene();

    @NotNull
    public abstract MutationType type();

    @Nullable
    public abstract String mutation();

}
