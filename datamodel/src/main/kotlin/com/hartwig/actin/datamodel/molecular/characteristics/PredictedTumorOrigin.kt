package com.hartwig.actin.datamodel.molecular.characteristics

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
