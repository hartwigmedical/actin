package com.hartwig.actin.molecular.datamodel.characteristics;

import java.util.Comparator;
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
        return bestPrediction().cancerType();
    }

    @Value.Derived
    public double likelihood() {
        return bestPrediction().likelihood();
    }

    @NotNull
    public abstract List<CupPrediction> predictions();

    @NotNull
    private CupPrediction bestPrediction() {
        return predictions().stream().max(Comparator.comparing(CupPrediction::likelihood)).orElseThrow();
    }
}
