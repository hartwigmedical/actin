package com.hartwig.actin.report.interpretation;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class EvaluatedTrial {

    @NotNull
    public abstract String trialId();

    @NotNull
    public abstract String acronym();

    @Nullable
    public abstract String cohort();

    @NotNull
    public abstract Set<String> molecularEvents();

    public abstract boolean isPotentiallyEligible();

    public abstract boolean isOpen();

    public abstract boolean hasSlotsAvailable();

    @NotNull
    public abstract Set<String> warnings();

    @NotNull
    public abstract Set<String> fails();

}
