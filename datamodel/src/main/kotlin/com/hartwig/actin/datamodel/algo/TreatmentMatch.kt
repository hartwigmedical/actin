package com.hartwig.actin.datamodel.algo

import com.fasterxml.jackson.annotation.JsonProperty
import com.hartwig.actin.datamodel.personalization.PersonalizedDataAnalysis
import java.time.LocalDate

data class TreatmentMatch(
    val patientId: String,
    val referenceDate: LocalDate,
    val referenceDateIsLive: Boolean,
    val trialMatches: List<TrialMatch>,
    val standardOfCareMatches: List<AnnotatedTreatmentMatch>?,
    val personalizedDataAnalysis: PersonalizedDataAnalysis?,
    val survivalPredictionsPerTreatment: Map<String, TreatmentEfficacyPrediction>?,
    val maxMolecularTestAge: LocalDate? = null
)

data class TreatmentEfficacyPrediction(
    @JsonProperty("survival_probs")
    val survivalProbs: List<Double>,

    @JsonProperty("shap_values")
    val shapValues: Map<String, ShapDetail>
) {
    data class ShapDetail(
        @JsonProperty("feature_value")
        val featureValue: Double,

        @JsonProperty("shap_value")
        val shapValue: Double
    )
}