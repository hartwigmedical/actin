package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.characteristics.CuppaPrediction
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableCuppaPrediction
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutablePredictedTumorOrigin
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter.greatestOmittedLikelihood
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter.hasConfidentPrediction
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter.interpret
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter.likelihoodMeetsConfidenceThreshold
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter.predictionsToDisplay
import com.hartwig.actin.report.pdf.util.Formats
import org.junit.Assert
import org.junit.Test
import java.util.stream.Collectors
import java.util.stream.IntStream

class TumorOriginInterpreterTest {
    @Test
    fun canDetermineConfidenceOfPredictedTumorOrigin() {
        Assert.assertFalse(hasConfidentPrediction(null))
        Assert.assertFalse(hasConfidentPrediction(withPredictions(0.4)))
        Assert.assertTrue(hasConfidentPrediction(withPredictions(0.8)))
        Assert.assertTrue(hasConfidentPrediction(withPredictions(0.99)))
    }

    @Test
    fun shouldReturnFalseForLikelihoodBelowConfidenceThreshold() {
        Assert.assertFalse(likelihoodMeetsConfidenceThreshold(0.5))
    }

    @Test
    fun shouldReturnTrueForLikelihoodThatMeetsConfidenceThreshold() {
        Assert.assertTrue(likelihoodMeetsConfidenceThreshold(0.8))
        Assert.assertTrue(likelihoodMeetsConfidenceThreshold(1.0))
    }

    @Test
    fun canInterpretPredictedTumorOrigins() {
        Assert.assertEquals(Formats.VALUE_UNKNOWN, interpret(null))
        Assert.assertEquals("type 1 (90%)", interpret(withPredictions(0.9)))
    }

    @Test
    fun shouldReturnEmptyListForDisplayWhenPredictedTumorOriginIsNull() {
        Assert.assertEquals(emptyList<Any>(), predictionsToDisplay(null))
    }

    @Test
    fun shouldReturnEmptyListForDisplayWhenAllPredictionsAreBelowThreshold() {
        Assert.assertEquals(emptyList<Any>(), predictionsToDisplay(withPredictions(0.09, 0.02, 0.05, 0.08)))
    }

    @Test
    fun shouldOmitPredictionsBelowThresholdForDisplay() {
        val predictions = predictionsToDisplay(withPredictions(0.4, 0.02, 0.05, 0.08))
        Assert.assertEquals(1, predictions.size.toLong())
        Assert.assertEquals(0.4, predictions.iterator().next().likelihood(), EPSILON)
    }

    @Test
    fun shouldDisplayAtMostThreePredictions() {
        val predictions = predictionsToDisplay(withPredictions(0.4, 0.12, 0.15, 0.25))
        Assert.assertEquals(3, predictions.size.toLong())
        Assert.assertEquals(setOf(0.4, 0.25, 0.15), predictions.stream().map { obj: CuppaPrediction -> obj.likelihood() }
            .collect(Collectors.toSet()))
    }

    @Test
    fun shouldReturnGreatestLikelihoodLimitedByThreshold() {
        Assert.assertEquals(0.08, greatestOmittedLikelihood(withPredictions(0.4, 0.02, 0.05, 0.08)), EPSILON)
    }

    @Test
    fun shouldReturnGreatestLikelihoodLimitedByCount() {
        Assert.assertEquals(0.12, greatestOmittedLikelihood(withPredictions(0.4, 0.12, 0.15, 0.25)), EPSILON)
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