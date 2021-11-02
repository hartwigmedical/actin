package com.hartwig.actin.algo.datamodel;

import java.util.List;
import java.util.Map;

import com.hartwig.actin.treatment.datamodel.EligibilityFunction;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TrialEligibility {

    @NotNull
    public abstract String trialId();

    @NotNull
    public abstract Map<EligibilityFunction, Evaluation> evaluations();

    @NotNull
    public abstract List<CohortEligibility> cohorts();
}
