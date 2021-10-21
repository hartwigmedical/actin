package com.hartwig.actin.treatment.database.config;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TrialConfig {

    @NotNull
    public abstract String trialId();

    @NotNull
    public abstract String acronym();

    @NotNull
    public abstract String title();
}
