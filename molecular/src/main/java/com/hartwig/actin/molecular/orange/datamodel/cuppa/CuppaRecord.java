package com.hartwig.actin.molecular.orange.datamodel.cuppa;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class CuppaRecord {

    @NotNull
    public abstract String predictedCancerType();

    public abstract double bestPredictionLikelihood();

}
