package com.hartwig.actin.report.interpretation.soc

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SurvivalPredictionEntryFactoryTest {

    @Test
    fun `Should create survival prediction entries when input is empty`() {
        assertThat(SurvivalPredictionEntryFactory.create(emptyMap())).isEmpty()
    }

    @Test
    fun `Should create survival prediction for input over limited days`() {
        val survivalPredictions = mapOf("treatment" to (1..90).map { 1.0 })

        val entries = SurvivalPredictionEntryFactory.create(survivalPredictions)

        assertThat(entries).containsExactly(
            SurvivalPredictionEntry(
                treatment = "treatment",
                day30SurvivalProbability = 1.0,
                day90SurvivalProbability = 1.0,
                day180SurvivalProbability = null,
                day360SurvivalProbability = null,
                day720SurvivalProbability = null
            )
        )
    }

    @Test
    fun `Should create survival prediction entries for comprehensive input`() {
        val survivalPredictions = mapOf(
            "No treatment" to (1..1000).map { 0.6 },
            "Pembrolizumab" to (1..1000).map { 1.0 / it }
        )

        val entries = SurvivalPredictionEntryFactory.create(survivalPredictions)

        assertThat(entries).containsExactly(
            SurvivalPredictionEntry(
                treatment = "No treatment",
                day30SurvivalProbability = 0.6,
                day90SurvivalProbability = 0.6,
                day180SurvivalProbability = 0.6,
                day360SurvivalProbability = 0.6,
                day720SurvivalProbability = 0.6
            ),
            SurvivalPredictionEntry(
                treatment = "Pembrolizumab",
                day30SurvivalProbability = 1.0 / 30,
                day90SurvivalProbability = 1.0 / 90,
                day180SurvivalProbability = 1.0 / 180,
                day360SurvivalProbability = 1.0 / 360,
                day720SurvivalProbability = 1.0 / 720
            )
        )
    }
}