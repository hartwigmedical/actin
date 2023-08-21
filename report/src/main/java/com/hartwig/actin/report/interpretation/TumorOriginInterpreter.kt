package com.hartwig.actin.report.interpretation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.molecular.datamodel.characteristics.CuppaPrediction;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.report.pdf.util.Formats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TumorOriginInterpreter {

    private static final double LIKELIHOOD_CONFIDENCE_THRESHOLD = 0.8;
    private static final double LIKELIHOOD_DISPLAY_THRESHOLD = 0.1;
    private static final int MAX_PREDICTIONS_TO_DISPLAY = 3;

    private TumorOriginInterpreter() {
    }

    public static boolean hasConfidentPrediction(@Nullable PredictedTumorOrigin predictedTumorOrigin) {
        return predictedTumorOrigin != null && likelihoodMeetsConfidenceThreshold(predictedTumorOrigin.likelihood());
    }

    public static boolean likelihoodMeetsConfidenceThreshold(double likelihood) {
        return Double.compare(likelihood, LIKELIHOOD_CONFIDENCE_THRESHOLD) >= 0;
    }

    @NotNull
    public static String interpret(@Nullable PredictedTumorOrigin predictedTumorOrigin) {
        if (predictedTumorOrigin == null) {
            return Formats.VALUE_UNKNOWN;
        }

        return predictedTumorOrigin.cancerType() + " (" + Formats.percentage(predictedTumorOrigin.likelihood()) + ")";
    }

    @NotNull
    public static List<CuppaPrediction> predictionsToDisplay(@Nullable PredictedTumorOrigin predictedTumorOrigin) {
        return predictedTumorOrigin == null
                ? Collections.emptyList()
                : streamNBestPredictions(predictedTumorOrigin, MAX_PREDICTIONS_TO_DISPLAY).filter(prediction -> prediction.likelihood()
                        > LIKELIHOOD_DISPLAY_THRESHOLD).collect(Collectors.toList());
    }

    public static double greatestOmittedLikelihood(@NotNull PredictedTumorOrigin predictedTumorOrigin) {
        List<Double> topLikelihoods =
                streamNBestPredictions(predictedTumorOrigin, MAX_PREDICTIONS_TO_DISPLAY + 1).map(CuppaPrediction::likelihood)
                        .collect(Collectors.toList());
        for (Double likelihood : topLikelihoods) {
            if (likelihood < LIKELIHOOD_DISPLAY_THRESHOLD) {
                return likelihood;
            }
        }
        return topLikelihoods.get(topLikelihoods.size() - 1);
    }

    @NotNull
    private static Stream<CuppaPrediction> streamNBestPredictions(@NotNull PredictedTumorOrigin predictedTumorOrigin, int limit) {
        return predictedTumorOrigin.predictions()
                .stream()
                .sorted(Comparator.comparing(CuppaPrediction::likelihood, Comparator.reverseOrder()))
                .limit(limit);
    }
}
