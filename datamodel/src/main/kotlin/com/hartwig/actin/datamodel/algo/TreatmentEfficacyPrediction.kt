package com.hartwig.actin.datamodel.algo

data class TreatmentEfficacyPrediction(
    val treatment: String,
    val survivalProbs: List<Double>,
    val shapValues: Map<String, ShapDetail>
) {
}