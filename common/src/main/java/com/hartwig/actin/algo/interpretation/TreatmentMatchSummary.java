package com.hartwig.actin.algo.interpretation;

import java.util.List;
import java.util.Map;

import com.hartwig.actin.treatment.datamodel.CohortMetadata;
import com.hartwig.actin.treatment.datamodel.TrialIdentification;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TreatmentMatchSummary {

    public abstract int trialCount();

    public abstract int cohortCount();

    @NotNull
    public abstract Map<TrialIdentification, List<CohortMetadata>> eligibleTrialMap();
}
