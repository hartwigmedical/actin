package com.hartwig.actin.report.interpretation;

import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;

import org.jetbrains.annotations.Nullable;

public final class TumorOriginInterpreter {

    private TumorOriginInterpreter() {
    }

    public static boolean hasConfidentPrediction(@Nullable PredictedTumorOrigin predictedTumorOrigin) {
        return predictedTumorOrigin != null && Double.compare(predictedTumorOrigin.likelihood(), 0.8) >= 0;
    }
}
