package com.hartwig.actin.datamodel.molecular.characteristics

data class PredictedTumorOrigin(val predictions: List<CupPrediction>) {
    
    fun cancerType(): String {
        return bestPrediction().cancerType
    }

    fun likelihood(): Double {
        return bestPrediction().likelihood
    }

    fun cuppaMode(): CuppaMode {
        return bestPrediction().cuppaMode
    }

    private fun bestPrediction(): CupPrediction {
        return predictions.maxBy(CupPrediction::likelihood)
    }
}
