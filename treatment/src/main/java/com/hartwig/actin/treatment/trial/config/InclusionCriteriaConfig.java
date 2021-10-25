package com.hartwig.actin.treatment.trial.config;

import java.util.Set;

import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class InclusionCriteriaConfig implements TrialConfig {

    @NotNull
    @Override
    public abstract String trialId();

    @NotNull
    public abstract Set<String> appliesToCohorts();

    @NotNull
    public abstract EligibilityRule eligibilityRule();

    @NotNull
    public abstract Set<String> eligibilityParameters();
}
