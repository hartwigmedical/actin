package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.molecular.datamodel.orange.characteristics.CupPrediction

data class PredictedTumorOrigin(val predictions: List<CupPrediction>) {
    
    fun cancerType(): String {
        return bestPrediction().cancerType
    }

    fun likelihood(): Double {
        return bestPrediction().likelihood
    }

    private fun bestPrediction(): CupPrediction {
        return predictions.maxBy(CupPrediction::likelihood)
    }
}
