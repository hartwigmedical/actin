package com.hartwig.actin.treatment.trial.config;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class CohortDefinitionConfig implements TrialConfig {

    @NotNull
    @Override
    public abstract String trialId();

    @NotNull
    public abstract String cohortId();

    public abstract boolean evaluable();

    public abstract boolean open();

    public abstract boolean slotsAvailable();

    public abstract boolean blacklist();

    @NotNull
    public abstract String description();
}
