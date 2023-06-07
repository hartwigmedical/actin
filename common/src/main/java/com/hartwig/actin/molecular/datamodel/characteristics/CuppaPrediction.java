package com.hartwig.actin.molecular.datamodel.characteristics;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class CuppaPrediction {

    @NotNull
    public abstract String cancerType();

    public abstract double likelihood();

    @Nullable
    public abstract Double snvPairwiseClassifier();

    @Nullable
    public abstract Double genomicPositionClassifier();

    @Nullable
    public abstract Double featureClassifier();
}
