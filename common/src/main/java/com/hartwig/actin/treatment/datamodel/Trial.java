package com.hartwig.actin.treatment.datamodel;

import java.util.List;

import com.hartwig.actin.algo.TrialInclusionFunction;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Trial {

    @NotNull
    public abstract String studyId();

    @NotNull
    public abstract List<TrialInclusionFunction> inclusionCriteria();

    @NotNull
    public abstract List<Cohort> cohorts();
}
