package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
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

    fun generateSummaryString(molecular: MolecularTest): String {
        val predictedTumorOrigin = molecular.characteristics.predictedTumorOrigin
        val wgsMolecular = if (molecular is MolecularRecord) molecular else null

        return if (predictedTumorOrigin != null && hasConfidentPrediction(predictedTumorOrigin) && wgsMolecular?.hasSufficientQuality == true) {
            predictedTumorOrigin.cancerType() + " (" + Formats.percentage(predictedTumorOrigin.likelihood()) + ")"
        } else if (wgsMolecular?.hasSufficientQuality == true && predictedTumorOrigin != null) {
            val predictionsMeetingThreshold = topPredictionsToDisplay(predictedTumorOrigin)
            if (predictionsMeetingThreshold.isEmpty()) {
                String.format(
                    "Inconclusive (%s %s)",
                    predictedTumorOrigin.cancerType(),
                    Formats.percentage(predictedTumorOrigin.likelihood())
                )
            } else {
                String.format("Inconclusive (%s)", predictionsMeetingThreshold.joinToString(", ") {
                    "${it.cancerType} ${Formats.percentage(it.likelihood)}"
                })
            }
        } else {
            Formats.VALUE_UNKNOWN
        }
    }

    fun topPredictionsToDisplay(predictedTumorOrigin: PredictedTumorOrigin?): List<CupPrediction> {
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