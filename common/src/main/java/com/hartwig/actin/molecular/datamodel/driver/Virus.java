package com.hartwig.actin.molecular.datamodel.driver;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Virus implements Driver {

    @NotNull
    public abstract String name();

    public abstract boolean isReliable();

    @Nullable
    public abstract String interpretation();

    public abstract int integrations();
}
