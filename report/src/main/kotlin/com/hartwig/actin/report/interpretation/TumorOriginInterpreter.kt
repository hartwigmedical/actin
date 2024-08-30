package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.PredictedTumorOrigin
import com.hartwig.actin.datamodel.molecular.orange.characteristics.CupPrediction
import com.hartwig.actin.report.pdf.util.Formats

object TumorOriginInterpreter {
    private const val LIKELIHOOD_CONFIDENCE_THRESHOLD = 0.8
    private const val LIKELIHOOD_DISPLAY_THRESHOLD = 0.1
    private const val MAX_PREDICTIONS_TO_DISPLAY = 3

    fun hasConfidentPrediction(predictedTumorOrigin: PredictedTumorOrigin?): Boolean {
        return predictedTumorOrigin != null && likelihoodMeetsConfidenceThreshold(predictedTumorOrigin.likelihood())
    }

    fun likelihoodMeetsConfidenceThreshold(likelihood: Double): Boolean {
        return likelihood.compareTo(LIKELIHOOD_CONFIDENCE_THRESHOLD) >= 0
    }

    fun interpret(predictedTumorOrigin: PredictedTumorOrigin?): String {
        return if (predictedTumorOrigin == null) {
            Formats.VALUE_UNKNOWN
        } else predictedTumorOrigin.cancerType() + " (" + Formats.percentage(predictedTumorOrigin.likelihood()) + ")"
    }

    fun predictionsToDisplay(predictedTumorOrigin: PredictedTumorOrigin?): List<CupPrediction> {
        return if (predictedTumorOrigin == null) emptyList() else bestNPredictions(predictedTumorOrigin, MAX_PREDICTIONS_TO_DISPLAY)
            .filter { it.likelihood > LIKELIHOOD_DISPLAY_THRESHOLD }
    }

    fun greatestOmittedLikelihood(predictedTumorOrigin: PredictedTumorOrigin): Double {
        val topLikelihoods = bestNPredictions(predictedTumorOrigin, MAX_PREDICTIONS_TO_DISPLAY + 1).map(CupPrediction::likelihood)
        return topLikelihoods.find { it < LIKELIHOOD_DISPLAY_THRESHOLD } ?: topLikelihoods.last()
    }

    private fun bestNPredictions(predictedTumorOrigin: PredictedTumorOrigin, limit: Int): List<CupPrediction> {
        return predictedTumorOrigin.predictions.sortedWith(compareByDescending(CupPrediction::likelihood)).take(limit)
    }
}