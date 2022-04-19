package com.hartwig.actin.report.interpretation;

import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.report.pdf.util.Formats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TumorOriginInterpreter {

    static final String INCONCLUSIVE_STRING = "Inconclusive";

    private TumorOriginInterpreter() {
    }

    public static boolean hasConfidentPrediction(@Nullable PredictedTumorOrigin predictedTumorOrigin) {
        return predictedTumorOrigin != null && Double.compare(predictedTumorOrigin.likelihood(), 0.8) >= 0;
    }

    @NotNull
    public static String interpret(@Nullable PredictedTumorOrigin predictedTumorOrigin) {
        if (predictedTumorOrigin == null) {
            return Formats.VALUE_UNKNOWN;
        }

        if (TumorOriginInterpreter.hasConfidentPrediction(predictedTumorOrigin)) {
            return predictedTumorOrigin.tumorType() + " (" + Formats.percentage(predictedTumorOrigin.likelihood()) + ")";
        } else {
            return INCONCLUSIVE_STRING;
        }
    }
}
