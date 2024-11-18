package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.PredictedTumorOrigin
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.orange.characteristics.CupPrediction
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TumorOriginInterpreterTest {
    private val inconclusivePredictions = listOf(
        CupPrediction(
            cancerType = "Melanoma",
            likelihood = 0.60,
            snvPairwiseClassifier = 0.979,
            genomicPositionClassifier = 0.99,
            featureClassifier = 0.972,
        ),
        CupPrediction(
            cancerType = "Lung",
            likelihood = 0.20,
            snvPairwiseClassifier = 0.0009,
            genomicPositionClassifier = 0.011,
            featureClassifier = 0.0102
        ),
    )
    private val inconclusiveInterpreter = TumorOriginInterpreter(PredictedTumorOrigin(inconclusivePredictions))
    private val conclusiveInterpreter =
        TumorOriginInterpreter(TestMolecularFactory.createProperTestMolecularRecord().characteristics.predictedTumorOrigin)

    @Test
    fun `Should determine confidence of predicted tumor origin`() {
        assertThat(TumorOriginInterpreter(null).hasConfidentPrediction()).isFalse
        assertThat(inconclusiveInterpreter.hasConfidentPrediction()).isFalse
        assertThat(withPredictions(0.8).hasConfidentPrediction()).isTrue
        assertThat(conclusiveInterpreter.hasConfidentPrediction()).isTrue
    }

    @Test
    fun `Should return empty list for display when predicted tumor origin is null`() {
        assertThat(TumorOriginInterpreter(null).topPredictionsToDisplay()).isEmpty()
    }

    @Test
    fun `Should return empty list for display when all predictions are below threshold`() {
        assertThat(withPredictions(0.09, 0.02, 0.05, 0.08).topPredictionsToDisplay()).isEmpty()
    }

    @Test
    fun `Should omit predictions below threshold for display`() {
        val predictions = withPredictions(0.4, 0.02, 0.05, 0.08).topPredictionsToDisplay()
        assertThat(predictions).hasSize(1)
        assertThat(predictions.first().likelihood).isEqualTo(0.4)
    }

    @Test
    fun `Should display at most three predictions`() {
        assertThat(withPredictions(0.4, 0.12, 0.15, 0.25).topPredictionsToDisplay().map(CupPrediction::likelihood))
            .containsExactly(0.4, 0.25, 0.15)
    }

    @Test
    fun `Should return greatest likelihood limited by threshold`() {
        assertThat(withPredictions(0.4, 0.02, 0.05, 0.08).greatestOmittedLikelihood()).isEqualTo(0.08)
    }

    @Test
    fun `Should return greatest likelihood limited by count`() {
        assertThat(withPredictions(0.4, 0.12, 0.15, 0.25).greatestOmittedLikelihood()).isEqualTo(0.12)
    }

    @Test
    fun `Should return one predicted tumor origin when conclusive with sufficient quality and purity`() {
        assertThat(conclusiveInterpreter.generateSummaryString(true)).isEqualTo("Melanoma (100%)")
    }

    @Test
    fun `Should add 'inconclusive' and show multiple tumor origins when inconclusive with sufficient quality and purity`() {
        assertThat(inconclusiveInterpreter.generateSummaryString(hasSufficientQuality = true))
            .isEqualTo("Inconclusive (Melanoma 60%, Lung 20%)")
    }

    @Test
    fun `Should add 'inconclusive' and show only predictions above threshold when inconclusive with sufficient quality and purity`() {
        assertThat(withPredictions(0.4, 0.02, 0.05, 0.08).generateSummaryString(true))
            .isEqualTo("Inconclusive (type 1 40%)")
    }

    @Test
    fun `Should add 'inconclusive' and show only top prediction when all predictions are below threshold`() {
        assertThat(withPredictions(0.09, 0.02, 0.05, 0.08).generateSummaryString(true))
            .isEqualTo("Inconclusive (type 1 9%)")
    }

    @Test
    fun `Should display at most three predictions in summary`() {
        assertThat(withPredictions(0.4, 0.12, 0.15, 0.25).generateSummaryString(true))
            .isEqualTo("Inconclusive (type 1 40%, type 4 25%, type 3 15%)")
    }

    @Test
    fun `Should return 'unknown' predicted tumor origin when conclusive with insufficient quality`() {
        assertThat(conclusiveInterpreter.generateSummaryString(hasSufficientQuality = false))
            .isEqualTo("Unknown")
    }

    @Test
    fun `Should return 'unknown' predicted tumor origin when inconclusive with insufficient quality and purity`() {
        assertThat(inconclusiveInterpreter.generateSummaryString(hasSufficientQuality = false))
            .isEqualTo("Unknown")
    }

    @Test
    fun `Should return 'unknown' predicted tumor origin when there is no prediction in molecular record`() {
        assertThat(TumorOriginInterpreter(null).generateSummaryString(true)).isEqualTo("Unknown")
    }

    private fun withPredictions(vararg likelihoods: Double): TumorOriginInterpreter {
        val predictions = likelihoods.mapIndexed { i, likelihood ->
            CupPrediction(
                cancerType = "type ${i + 1}",
                likelihood = likelihood,
                snvPairwiseClassifier = likelihood,
                genomicPositionClassifier = likelihood,
                featureClassifier = likelihood
            )
        }
        return TumorOriginInterpreter(PredictedTumorOrigin(predictions))
    }
}