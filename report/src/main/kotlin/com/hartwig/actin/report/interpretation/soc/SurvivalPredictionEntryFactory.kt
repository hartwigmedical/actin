package com.hartwig.actin.report.interpretation.soc

object SurvivalPredictionEntryFactory {

    fun create(survivalPredictions: Map<String, List<Double>>): List<SurvivalPredictionEntry> {
        return survivalPredictions.map { (key, value) ->
            SurvivalPredictionEntry(
                treatment = key,
                day30SurvivalProbability = survivalAfterDays(value, 30),
                day90SurvivalProbability = survivalAfterDays(value, 90),
                day180SurvivalProbability = survivalAfterDays(value, 180),
                day360SurvivalProbability = survivalAfterDays(value, 360),
                day720SurvivalProbability = survivalAfterDays(value, 720)
            )
        }
    }

    private fun survivalAfterDays(survivalProbabilities: List<Double>, afterDays: Int): Double? {
        return if (survivalProbabilities.size >= afterDays) survivalProbabilities[afterDays - 1] else null
    }
}