package com.hartwig.actin.molecular.datamodel.characteristics

import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin.cancerType
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin.likelihood
import org.junit.Assert
import org.junit.Test
import java.util.function.IntFunction
import java.util.stream.Collectors
import java.util.stream.IntStream

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
            return ImmutablePredictedTumorOrigin.builder()
                .predictions(
                    IntStream.range(0, likelihoods.size)
                        .mapToObj<Any>(IntFunction<Any> { i: Int ->
                            ImmutableCupPrediction.builder()
                                .cancerType(String.format("type %s", i + 1))
                                .likelihood(likelihoods[i])
                                .snvPairwiseClassifier(likelihoods[i])
                                .genomicPositionClassifier(likelihoods[i])
                                .featureClassifier(likelihoods[i])
                                .build()
                        })
                        .collect(Collectors.toList())
                )
                .build()
        }
    }
}