package com.hartwig.actin.molecular.datamodel.evidence;

import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ActinTrialEvidence implements EvidenceEntry {

    @Override
    @NotNull
    public abstract String event();

    @NotNull
    public abstract String trialAcronym();

    @Nullable
    public abstract String cohortId();

    public abstract boolean isInclusionCriterion();

    @NotNull
    public abstract EligibilityRule rule();

    @Nullable
    public abstract String gene();

    @Nullable
    public abstract String mutation();

}
