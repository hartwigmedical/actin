package com.hartwig.actin.molecular.datamodel.hmf.characteristics

data class CupPrediction(
    val cancerType: String,
    val likelihood: Double,
    val snvPairwiseClassifier: Double,
    val genomicPositionClassifier: Double,
    val featureClassifier: Double
)