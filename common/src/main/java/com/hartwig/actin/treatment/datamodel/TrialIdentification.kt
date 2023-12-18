package com.hartwig.actin.treatment.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TrialIdentification {

    @NotNull
    public abstract String trialId();

    public abstract boolean open();

    @NotNull
    public abstract String acronym();

    @NotNull
    public abstract String title();
}
