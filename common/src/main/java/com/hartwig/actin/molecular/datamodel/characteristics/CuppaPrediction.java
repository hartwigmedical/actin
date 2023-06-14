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

    public abstract double snvPairwiseClassifier();

    public abstract double genomicPositionClassifier();

    public abstract double featureClassifier();
}
