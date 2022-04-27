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

    public abstract boolean hasMolecularEvidence();

    @Nullable
    public abstract String cohort();

    public abstract boolean isPotentiallyEligible();

    public abstract boolean isOpenAndHasSlotsAvailable();

    @NotNull
    public abstract Set<String> warnings();

    @NotNull
    public abstract Set<String> fails();

}
