package com.hartwig.actin.report.interpretation.soc

object SurvivalPredictionEntryFactory {

    fun create(survivalPredictions: Map<String, List<Double>>): List<SurvivalPredictionEntry> {
        return survivalPredictions.map { (treatment, dailySurvivalProbabilities) ->
            SurvivalPredictionEntry(
                treatment = treatment,
                day30SurvivalProbability = survivalAfterDays(dailySurvivalProbabilities, 30),
                day90SurvivalProbability = survivalAfterDays(dailySurvivalProbabilities, 90),
                day180SurvivalProbability = survivalAfterDays(dailySurvivalProbabilities, 180),
                day360SurvivalProbability = survivalAfterDays(dailySurvivalProbabilities, 360),
                day720SurvivalProbability = survivalAfterDays(dailySurvivalProbabilities, 720)
            )
        }
    }

    private fun survivalAfterDays(dailySurvivalProbabilities: List<Double>, afterDays: Int): Double? {
        return if (dailySurvivalProbabilities.size >= afterDays) dailySurvivalProbabilities[afterDays - 1] else null
    }
}