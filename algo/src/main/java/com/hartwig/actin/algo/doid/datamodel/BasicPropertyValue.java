package com.hartwig.actin.algo.doid.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(allParameters = true,
             passAnnotations = { NotNull.class, Nullable.class })
public abstract class BasicPropertyValue {

    @NotNull
    public abstract String pred();

    @NotNull
    public abstract String val();
}
