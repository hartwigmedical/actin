package com.hartwig.actin.datamodel.molecular.characteristics

data class CupPrediction(
    val cancerType: String,
    val likelihood: Double,
    val snvPairwiseClassifier: Double,
    val genomicPositionClassifier: Double,
    val featureClassifier: Double,
    val cuppaMode: CuppaMode? = null
)