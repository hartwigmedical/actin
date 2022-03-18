package com.hartwig.actin.algo.datamodel;

import java.util.List;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TreatmentMatch {

    @NotNull
    public abstract String sampleId();

    @NotNull
    public abstract List<TrialMatch> trialMatches();
}
