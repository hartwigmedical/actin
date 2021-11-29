package com.hartwig.actin.serve.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ServeRecord {

    @NotNull
    public abstract String trialId();

    @NotNull
    public abstract String event();
}
