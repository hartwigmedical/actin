package com.hartwig.actin.molecular.datamodel.characteristics;

import java.util.List;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class PredictedTumorOrigin {

    @NotNull
    @Value.Derived
    public String cancerType() {
        return predictions().get(0).cancerType();
    }

    @Value.Derived
    public double likelihood() {
        return predictions().get(0).likelihood();
    }

    @NotNull
    public abstract List<CuppaPrediction> predictions();
}
