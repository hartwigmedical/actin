package com.hartwig.actin.molecular.datamodel.mapping;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class InactivatedGene {

    @NotNull
    public abstract String gene();

    public abstract boolean hasBeenDeleted();
}
