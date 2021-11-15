package com.hartwig.actin.algo.datamodel;

import java.util.Map;

import com.hartwig.actin.treatment.datamodel.Eligibility;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class CohortEligibility {

    @NotNull
    public abstract String cohortId();

    @NotNull
    public abstract Evaluation overallEvaluation();

    @NotNull
    public abstract Map<Eligibility, Evaluation> evaluations();
}
