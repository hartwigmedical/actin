package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.characteristics.ImmutablePredictedTumorOrigin
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter.greatestOmittedLikelihood
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter.hasConfidentPrediction
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter.interpret
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter.likelihoodMeetsConfidenceThreshold
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter.predictionsToDisplay
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.hmftools.datamodel.cuppa.CuppaPrediction
import com.hartwig.hmftools.datamodel.cuppa.ImmutableCuppaPrediction
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.Test
import java.util.stream.Collectors
import java.util.stream.IntStream

class TumorOriginInterpreterTest {
    @Test
    fun canDetermineConfidenceOfPredictedTumorOrigin() {
        assertThat(hasConfidentPrediction(null)).isFalse
        assertThat(hasConfidentPrediction(withPredictions(0.4))).isFalse
        assertThat(hasConfidentPrediction(withPredictions(0.8))).isTrue
        assertThat(hasConfidentPrediction(withPredictions(0.99))).isTrue
    }

    @Test
    fun shouldReturnFalseForLikelihoodBelowConfidenceThreshold() {
        assertThat(likelihoodMeetsConfidenceThreshold(0.5)).isFalse
    }

    @Test
    fun shouldReturnTrueForLikelihoodThatMeetsConfidenceThreshold() {
        assertThat(likelihoodMeetsConfidenceThreshold(0.8)).isTrue
        assertThat(likelihoodMeetsConfidenceThreshold(1.0)).isTrue
    }

    @Test
    fun canInterpretPredictedTumorOrigins() {
        assertThat(interpret(null)).isEqualTo(Formats.VALUE_UNKNOWN)
        assertThat(interpret(withPredictions(0.9))).isEqualTo("type 1 (90%)")
    }

    @Test
    fun shouldReturnEmptyListForDisplayWhenPredictedTumorOriginIsNull() {
        assertThat(predictionsToDisplay(null)).isEmpty()
    }

    @Test
    fun shouldReturnEmptyListForDisplayWhenAllPredictionsAreBelowThreshold() {
        assertThat(predictionsToDisplay(withPredictions(0.09, 0.02, 0.05, 0.08))).isEmpty()
    }

    @Test
    fun shouldOmitPredictionsBelowThresholdForDisplay() {
        val predictions = predictionsToDisplay(withPredictions(0.4, 0.02, 0.05, 0.08))
        assertThat(predictions).hasSize(1)
        assertThat(predictions.iterator().next().likelihood()).isCloseTo(0.4, within(EPSILON))
    }

    @Test
    fun shouldDisplayAtMostThreePredictions() {
        val predictions = predictionsToDisplay(withPredictions(0.4, 0.12, 0.15, 0.25))
        assertThat(predictions.map(CuppaPrediction::likelihood)).containsExactlyInAnyOrder(0.4, 0.25, 0.15)
    }

    @Test
    fun shouldReturnGreatestLikelihoodLimitedByThreshold() {
        assertThat(greatestOmittedLikelihood(withPredictions(0.4, 0.02, 0.05, 0.08))).isCloseTo(0.08, within(EPSILON))
    }

    @Test
    fun shouldReturnGreatestLikelihoodLimitedByCount() {
        assertThat(greatestOmittedLikelihood(withPredictions(0.4, 0.12, 0.15, 0.25))).isCloseTo(0.12, within(EPSILON))
    }

    companion object {
        const val EPSILON = 0.0001
        private fun withPredictions(vararg likelihoods: Double): PredictedTumorOrigin {
            return ImmutablePredictedTumorOrigin.builder()
                .predictions(IntStream.range(0, likelihoods.size)
                    .mapToObj { i: Int ->
                        ImmutableCuppaPrediction.builder()
                            .cancerType(String.format("type %s", i + 1))
                            .likelihood(likelihoods[i])
                            .snvPairwiseClassifier(likelihoods[i])
                            .genomicPositionClassifier(likelihoods[i])
                            .featureClassifier(likelihoods[i])
                            .build()
                    }
                    .collect(Collectors.toList()))
                .build()
        }
    }
}