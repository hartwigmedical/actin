package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.PredictedTumorOrigin
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.orange.characteristics.CupPrediction
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter.greatestOmittedLikelihood
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter.hasConfidentPrediction
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter.likelihoodMeetsConfidenceThreshold
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter.generateDetailsPredictions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.Test

const val EPSILON = 0.0001

class TumorOriginInterpreterTest {
    private val molecularRecord = TestMolecularFactory.createProperTestMolecularRecord()
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
    private val inconclusiveCharacteristics =
        molecularRecord.characteristics.copy(predictedTumorOrigin = PredictedTumorOrigin(inconclusivePredictions))

    @Test
    fun `Can determine confidence of predicted tumor origin`() {
        assertThat(hasConfidentPrediction(null)).isFalse
        assertThat(hasConfidentPrediction(withPredictions(0.4))).isFalse
        assertThat(hasConfidentPrediction(withPredictions(0.8))).isTrue
        assertThat(hasConfidentPrediction(withPredictions(0.99))).isTrue
    }

    @Test
    fun `Should return false for likelihood below confidence threshold`() {
        assertThat(likelihoodMeetsConfidenceThreshold(0.5)).isFalse
    }

    @Test
    fun `Should return true for likelihood that meets confidence threshold`() {
        assertThat(likelihoodMeetsConfidenceThreshold(0.8)).isTrue
        assertThat(likelihoodMeetsConfidenceThreshold(1.0)).isTrue
    }

    @Test
    fun `Should return empty list for display when predicted tumor origin is null`() {
        assertThat(generateDetailsPredictions(null)).isEmpty()
    }

    @Test
    fun `Should return empty list for display when all predictions are below threshold`() {
        assertThat(generateDetailsPredictions(withPredictions(0.09, 0.02, 0.05, 0.08))).isEmpty()
    }

    @Test
    fun `Should omit predictions below threshold for display`() {
        val predictions = generateDetailsPredictions(withPredictions(0.4, 0.02, 0.05, 0.08))
        assertThat(predictions).hasSize(1)
        assertThat(predictions.iterator().next().likelihood).isCloseTo(0.4, within(EPSILON))
    }

    @Test
    fun `Should display at most three predictions`() {
        val predictions = generateDetailsPredictions(withPredictions(0.4, 0.12, 0.15, 0.25))
        assertThat(predictions.map(CupPrediction::likelihood)).containsExactlyInAnyOrder(0.4, 0.25, 0.15)
    }

    @Test
    fun `Should return greatest likelihood limited by threshold`() {
        assertThat(greatestOmittedLikelihood(withPredictions(0.4, 0.02, 0.05, 0.08))).isCloseTo(0.08, within(EPSILON))
    }

    @Test
    fun `Should return greatest likelihood limited by count`() {
        assertThat(greatestOmittedLikelihood(withPredictions(0.4, 0.12, 0.15, 0.25))).isCloseTo(0.12, within(EPSILON))
    }

    @Test
    fun `Should return one predicted tumor origin when conclusive with sufficient quality and purity`() {
        val string = TumorOriginInterpreter.generateSummaryString(molecular = molecularRecord)
        assertThat(string).isEqualTo("Melanoma (100%)")
    }

    @Test
    fun `Should add 'inconclusive' and show multiple tumor origins when inconclusive with sufficient quality and purity`() {
        val string =
            TumorOriginInterpreter.generateSummaryString(molecular = molecularRecord.copy(characteristics = inconclusiveCharacteristics))
        assertThat(string).isEqualTo("Inconclusive (Melanoma 60%, Lung 20%)")
    }

    @Test
    fun `Should return 'unknown' predicted tumor origin when conclusive with insufficient quality and purity`() {
        val string = TumorOriginInterpreter.generateSummaryString(
            molecular = molecularRecord.copy(
                hasSufficientPurity = false,
                hasSufficientQuality = false
            )
        )
        assertThat(string).isEqualTo("Unknown")
    }

    @Test
    fun `Should return 'unknown' predicted tumor origin when inconclusive with insufficient quality and purity`() {
        val string = TumorOriginInterpreter.generateSummaryString(
            molecular = molecularRecord.copy(characteristics = inconclusiveCharacteristics)
                .copy(hasSufficientPurity = false, hasSufficientQuality = false)
        )
        assertThat(string).isEqualTo("Unknown")
    }

    @Test
    fun `Should return 'unknown' predicted tumor origin when there is no prediction in molecular record`() {
        val string = TumorOriginInterpreter.generateSummaryString(
            molecular = TestMolecularFactory.createMinimalTestMolecularRecord()
        )
        assertThat(string).isEqualTo("Unknown")
    }

    private fun withPredictions(vararg likelihoods: Double): PredictedTumorOrigin {
        return PredictedTumorOrigin(
            predictions = likelihoods.mapIndexed { i, likelihood ->
                CupPrediction(
                    cancerType = String.format("type %s", i + 1),
                    likelihood = likelihood,
                    snvPairwiseClassifier = likelihood,
                    genomicPositionClassifier = likelihood,
                    featureClassifier = likelihood
                )
            }
        )
    }
}