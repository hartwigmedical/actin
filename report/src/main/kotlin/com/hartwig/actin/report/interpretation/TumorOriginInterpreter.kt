package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.interpretation.TumorOriginInterpreter
import com.hartwig.actin.report.pdf.util.Formats

fun TumorOriginInterpreter.generateSummaryString(): String {
    val predictedTumorOrigin = predictedTumorOrigin
    return when {
        predictedTumorOrigin == null || hasSufficientQuality != true -> {
            Formats.VALUE_UNKNOWN
        }

        hasConfidentPrediction() -> {
            "${predictedTumorOrigin.cancerType()} (${Formats.percentage(predictedTumorOrigin.likelihood())})"
        }

        else -> {
            val predictionsMeetingThreshold = topPredictionsToDisplay().map { it.cancerType to it.likelihood }
                .ifEmpty { listOf(predictedTumorOrigin.cancerType() to predictedTumorOrigin.likelihood()) }
                .joinToString(", ") { "${it.first} ${Formats.percentage(it.second)}" }

            "Inconclusive (${predictionsMeetingThreshold})"
        }
    }
}