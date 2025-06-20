package com.hartwig.actin.report.interpretation.soc

data class SurvivalPredictionEntry(
    val treatment: String,
    val day30SurvivalProbability: Double?,
    val day90SurvivalProbability: Double?,
    val day180SurvivalProbability: Double?,
    val day360SurvivalProbability: Double?,
    val day720SurvivalProbability: Double?
)