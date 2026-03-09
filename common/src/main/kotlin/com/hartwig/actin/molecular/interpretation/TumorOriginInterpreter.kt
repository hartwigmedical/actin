package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.characteristics.CupPrediction
import com.hartwig.actin.datamodel.molecular.characteristics.PredictedTumorOrigin

private const val LIKELIHOOD_CONFIDENCE_THRESHOLD = 0.8
private const val LIKELIHOOD_DISPLAY_THRESHOLD = 0.1
private const val MAX_PREDICTIONS_TO_DISPLAY = 3

class TumorOriginInterpreter(val hasSufficientQuality: Boolean?, val predictedTumorOrigin: PredictedTumorOrigin?) {

    fun hasConfidentPrediction(): Boolean {
        return hasSufficientQuality == true && predictedTumorOrigin?.likelihood()?.let { it >= LIKELIHOOD_CONFIDENCE_THRESHOLD } == true
    }

    fun topPredictionsToDisplay(): List<CupPrediction> = bestNPredictions(MAX_PREDICTIONS_TO_DISPLAY)
        ?.filter { it.likelihood > LIKELIHOOD_DISPLAY_THRESHOLD }
        ?: emptyList()

    fun greatestOmittedLikelihood(): Double = bestNPredictions(MAX_PREDICTIONS_TO_DISPLAY + 1)
        ?.map(CupPrediction::likelihood)
        ?.let { topLikelihoods -> topLikelihoods.firstOrNull { it < LIKELIHOOD_DISPLAY_THRESHOLD } ?: topLikelihoods.last() }
        ?: Double.NaN

    private fun bestNPredictions(limit: Int): List<CupPrediction>? = predictedTumorOrigin?.predictions
        ?.sortedWith(compareByDescending(CupPrediction::likelihood))
        ?.take(limit)

    companion object {
        fun create(molecular: MolecularTest): TumorOriginInterpreter {
            return create(listOf(molecular))
        }

        fun create(molecular: List<MolecularTest>): TumorOriginInterpreter {
            val wgsMolecular = MolecularHistory(molecular).latestOrangeMolecularRecord()

            return TumorOriginInterpreter(
                hasSufficientQuality = wgsMolecular?.hasSufficientQuality,
                predictedTumorOrigin = wgsMolecular?.characteristics?.predictedTumorOrigin
            )
        }
    }
}