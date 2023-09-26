package com.hartwig.actin.molecular.datamodel.characteristics;

import java.util.Comparator;
import java.util.List;

import com.hartwig.hmftools.datamodel.cuppa.CuppaPrediction;

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
    public abstract List<CuppaPrediction> predictions();

    @NotNull
    private CuppaPrediction bestPrediction() {
        return predictions().stream().max(Comparator.comparing(CuppaPrediction::likelihood)).orElseThrow();
    }
}
