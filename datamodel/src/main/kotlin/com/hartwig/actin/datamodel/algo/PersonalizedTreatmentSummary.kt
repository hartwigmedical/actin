package com.hartwig.actin.datamodel.algo

data class PersonalizedTreatmentSummary(
    val predictions: List<TreatmentEfficacyPrediction>?,
    val similarPatientsSummary: SimilarPatientsSummary?
)

data class SimilarPatientsSummary(
    val overallTreatmentProportion: List<TreatmentProportion>,
    val similarPatientsTreatmentProportion: List<TreatmentProportion>,
    val similarPatientsFeatureDistribution: List<FeatureDistribution>,
)

data class TreatmentEfficacyPrediction(
    val treatment: String,
    val survivalProbs: List<Double>,
    val shapValues: Map<String, ShapDetail>
)

data class ShapDetail(
    val featureValue: Double,
    val shapValue: Double
)

data class TreatmentProportion(
    val treatment: String,
    val proportion: Double
)

data class FeatureDistribution(
    val feature: String,
    val patientValue: String,
    val neighborsDistribution: Map<String, Double>,
    val overallDistribution: Map<String, Double>
)