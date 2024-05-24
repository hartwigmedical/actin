package com.hartwig.actin.molecular.datamodel.wgs.characteristics

data class CupPrediction(
    val cancerType: String,
    val likelihood: Double,
    val snvPairwiseClassifier: Double,
    val genomicPositionClassifier: Double,
    val featureClassifier: Double
)