package com.hartwig.actin.algo.datamodel;

import java.util.List;
import java.util.Map;

import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.TrialIdentification;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TrialEligibility {

    @NotNull
    public abstract TrialIdentification identification();

    @NotNull
    public abstract Evaluation overallEvaluation();

    @NotNull
    public abstract Map<Eligibility, Evaluation> evaluations();

    @NotNull
    public abstract List<CohortEligibility> cohorts();
}
