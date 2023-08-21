package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.characteristics.CuppaPrediction
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin
import com.hartwig.actin.report.pdf.util.Formats
import java.util.stream.Collectors
import java.util.stream.Stream

object TumorOriginInterpreter {
    private const val LIKELIHOOD_CONFIDENCE_THRESHOLD = 0.8
    private const val LIKELIHOOD_DISPLAY_THRESHOLD = 0.1
    private const val MAX_PREDICTIONS_TO_DISPLAY = 3
    fun hasConfidentPrediction(predictedTumorOrigin: PredictedTumorOrigin?): Boolean {
        return predictedTumorOrigin != null && likelihoodMeetsConfidenceThreshold(predictedTumorOrigin.likelihood())
    }

    fun likelihoodMeetsConfidenceThreshold(likelihood: Double): Boolean {
        return java.lang.Double.compare(likelihood, LIKELIHOOD_CONFIDENCE_THRESHOLD) >= 0
    }

    fun interpret(predictedTumorOrigin: PredictedTumorOrigin?): String {
        return if (predictedTumorOrigin == null) {
            Formats.VALUE_UNKNOWN
        } else predictedTumorOrigin.cancerType() + " (" + Formats.percentage(predictedTumorOrigin.likelihood()) + ")"
    }

    fun predictionsToDisplay(predictedTumorOrigin: PredictedTumorOrigin?): List<CuppaPrediction> {
        return if (predictedTumorOrigin == null) emptyList() else streamNBestPredictions(
            predictedTumorOrigin,
            MAX_PREDICTIONS_TO_DISPLAY
        ).filter { prediction: CuppaPrediction ->
            (prediction.likelihood()
                    > LIKELIHOOD_DISPLAY_THRESHOLD)
        }.collect(Collectors.toList())
    }

    fun greatestOmittedLikelihood(predictedTumorOrigin: PredictedTumorOrigin): Double {
        val topLikelihoods =
            streamNBestPredictions(predictedTumorOrigin, MAX_PREDICTIONS_TO_DISPLAY + 1).map { obj: CuppaPrediction -> obj.likelihood() }
                .collect(Collectors.toList())
        for (likelihood in topLikelihoods) {
            if (likelihood < LIKELIHOOD_DISPLAY_THRESHOLD) {
                return likelihood
            }
        }
        return topLikelihoods[topLikelihoods.size - 1]
    }

    private fun streamNBestPredictions(predictedTumorOrigin: PredictedTumorOrigin, limit: Int): Stream<CuppaPrediction> {
        return predictedTumorOrigin.predictions()
            .stream()
            .sorted(Comparator.comparing({ obj: CuppaPrediction -> obj.likelihood() }, Comparator.reverseOrder()))
            .limit(limit.toLong())
    }
}