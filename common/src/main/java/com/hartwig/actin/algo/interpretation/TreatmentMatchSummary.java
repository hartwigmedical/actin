package com.hartwig.actin.algo.interpretation;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TreatmentMatchSummary {

    public abstract int trialCount();

    public abstract int eligibleTrialCount();

    @NotNull
    public abstract Set<String> eligibleTrials();

    public abstract int cohortCount();

    public abstract int eligibleCohortCount();

    public abstract int eligibleOpenCohortCount();

    @NotNull
    public abstract Set<String> eligibleOpenCohorts();
}
