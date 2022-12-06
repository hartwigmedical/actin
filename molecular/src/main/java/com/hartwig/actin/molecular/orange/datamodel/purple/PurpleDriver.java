package com.hartwig.actin.molecular.orange.datamodel.purple;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class PurpleDriver {

    @NotNull
    public abstract String gene();

    @NotNull
    public abstract String transcript();

    @NotNull
    public abstract PurpleDriverType type();

    public abstract double driverLikelihood();

}
