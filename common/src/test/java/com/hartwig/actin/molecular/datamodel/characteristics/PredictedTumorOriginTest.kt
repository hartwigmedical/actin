package com.hartwig.actin.molecular.datamodel.characteristics

import org.junit.Assert
import org.junit.Test

class PredictedTumorOriginTest {
    @Test
    fun shouldIdentifyBestPredictionInUnsortedList() {
        val predictedTumorOrigin = withPredictions(0.1, 0.08, 0.4, 0.2)
        Assert.assertEquals("type 3", predictedTumorOrigin.cancerType())
        Assert.assertEquals(0.4, predictedTumorOrigin.likelihood(), EPSILON)
    }

    companion object {
        private const val EPSILON = 0.001

        private fun withPredictions(vararg likelihoods: Double): PredictedTumorOrigin {
            return PredictedTumorOrigin(
                predictions = likelihoods.mapIndexed { i, likelihood ->
                    CupPrediction(
                        cancerType = String.format("type %s", i + 1),
                        likelihood = likelihood,
                        snvPairwiseClassifier = likelihood,
                        genomicPositionClassifier = likelihood,
                        featureClassifier = likelihood,
                    )
                }
            )
        }
    }
}