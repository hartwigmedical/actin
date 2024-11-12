package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.PredictedTumorOrigin
import com.hartwig.actin.datamodel.molecular.orange.characteristics.CupPrediction
import com.hartwig.actin.report.pdf.util.Formats

private const val LIKELIHOOD_CONFIDENCE_THRESHOLD = 0.8
private const val LIKELIHOOD_DISPLAY_THRESHOLD = 0.1
private const val MAX_PREDICTIONS_TO_DISPLAY = 3

class TumorOriginInterpreter(private val predictedTumorOrigin: PredictedTumorOrigin?) {

    fun hasConfidentPrediction(): Boolean {
        return predictedTumorOrigin?.likelihood()?.let { it >= LIKELIHOOD_CONFIDENCE_THRESHOLD } == true
    }

    fun generateSummaryString(hasSufficientQuality: Boolean?): String {
        return when {
            predictedTumorOrigin == null || hasSufficientQuality != true -> {
                Formats.VALUE_UNKNOWN
            }
            hasConfidentPrediction() -> {
                predictedTumorOrigin.cancerType() + " (" + Formats.percentage(predictedTumorOrigin.likelihood()) + ")"
            }
            else -> {
                val predictionsMeetingThreshold = topPredictions().map { it.cancerType to it.likelihood }
                    .ifEmpty { listOf(predictedTumorOrigin.cancerType() to predictedTumorOrigin.likelihood()) }
                    .joinToString(", ") { "${it.first} ${Formats.percentage(it.second)}" }

                "Inconclusive (${predictionsMeetingThreshold})"
            }
        }
    }

    fun topPredictions(): List<CupPrediction> = bestNPredictions(MAX_PREDICTIONS_TO_DISPLAY)
        ?.filter { it.likelihood > LIKELIHOOD_DISPLAY_THRESHOLD }
        ?: emptyList()

    fun greatestOmittedLikelihood(): Double = bestNPredictions(MAX_PREDICTIONS_TO_DISPLAY + 1)
        ?.map(CupPrediction::likelihood)
        ?.let { topLikelihoods -> topLikelihoods.firstOrNull { it < LIKELIHOOD_DISPLAY_THRESHOLD } ?: topLikelihoods.last() }
        ?: Double.NaN

    private fun bestNPredictions(limit: Int): List<CupPrediction>? {
        return predictedTumorOrigin?.predictions
            ?.sortedWith(compareByDescending(CupPrediction::likelihood))
            ?.take(limit)
    }
}