package com.hartwig.actin.doid.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(allParameters = true,
             passAnnotations = { NotNull.class, Nullable.class })
public abstract class Restriction {

    @NotNull
    public abstract String propertyId();

    @NotNull
    public abstract String fillerId();
}
