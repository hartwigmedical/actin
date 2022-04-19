package com.hartwig.actin.molecular.datamodel.driver;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Virus implements Actionable {

    @NotNull
    public abstract String name();

    @NotNull
    public abstract String details();

    @NotNull
    public abstract DriverLikelihood driverLikelihood();
}
