package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.hmftools.datamodel.cuppa.CuppaPrediction

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

    fun predictionsToDisplay(predictedTumorOrigin: PredictedTumorOrigin?): List<CuppaPrediction> {
        return if (predictedTumorOrigin == null) emptyList() else bestNPredictions(predictedTumorOrigin, MAX_PREDICTIONS_TO_DISPLAY)
            .filter { it.likelihood() > LIKELIHOOD_DISPLAY_THRESHOLD }
    }

    fun greatestOmittedLikelihood(predictedTumorOrigin: PredictedTumorOrigin): Double {
        val topLikelihoods = bestNPredictions(predictedTumorOrigin, MAX_PREDICTIONS_TO_DISPLAY + 1).map(CuppaPrediction::likelihood)
        return topLikelihoods.find { it < LIKELIHOOD_DISPLAY_THRESHOLD } ?: topLikelihoods.last()
    }

    private fun bestNPredictions(predictedTumorOrigin: PredictedTumorOrigin, limit: Int): List<CuppaPrediction> {
        return predictedTumorOrigin.predictions().sortedWith(compareByDescending(CuppaPrediction::likelihood)).take(limit)
    }
}