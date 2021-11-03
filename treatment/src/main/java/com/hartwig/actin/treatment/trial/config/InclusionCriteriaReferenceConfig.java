package com.hartwig.actin.treatment.trial.config;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class InclusionCriteriaReferenceConfig implements TrialConfig {

    @NotNull
    @Override
    public abstract String trialId();

    @NotNull
    public abstract String criterionId();

    @NotNull
    public abstract String criterionText();
}
