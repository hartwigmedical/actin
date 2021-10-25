package com.hartwig.actin.treatment.datamodel;

import java.util.List;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Trial {

    @NotNull
    public abstract String trialId();

    @NotNull
    public abstract String acronym();

    @NotNull
    public abstract String title();

    @NotNull
    public abstract List<EligibilityFunction> generalEligibilityFunctions();

    @NotNull
    public abstract List<Cohort> cohorts();

}
