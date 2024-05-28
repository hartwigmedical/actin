package com.hartwig.actin.molecular.datamodel.characteristics

import com.hartwig.actin.molecular.datamodel.PredictedTumorOrigin
import com.hartwig.actin.molecular.datamodel.hmf.characteristics.CupPrediction
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test

class PredictedTumorOriginTest {

    private val epsilon = 0.001

    @Test
    fun `Should identify best prediction from unsorted list`() {
        val predictedTumorOrigin = withPredictions(0.1, 0.08, 0.4, 0.2)
        assertThat(predictedTumorOrigin.cancerType()).isEqualTo("type 3")
        assertThat(predictedTumorOrigin.likelihood()).isEqualTo(0.4, Offset.offset(epsilon))
    }

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