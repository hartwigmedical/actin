package com.hartwig.actin.treatment.datamodel;

import java.util.List;

import com.hartwig.actin.algo.eligibility.EligibilityFunction;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Cohort {

    @NotNull
    public abstract String cohortId();

    public abstract boolean open();

    @NotNull
    public abstract String description();

    @NotNull
    public abstract List<EligibilityFunction> inclusionCriteria();
}
