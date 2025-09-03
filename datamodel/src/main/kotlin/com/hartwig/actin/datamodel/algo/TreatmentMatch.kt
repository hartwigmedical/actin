package com.hartwig.actin.datamodel.algo

import com.hartwig.actin.datamodel.personalization.PersonalizedDataAnalysis
import java.time.LocalDate

data class TreatmentMatch(
    val patientId: String,
    val referenceDate: LocalDate,
    val referenceDateIsLive: Boolean,
    val trialMatches: List<TrialMatch>,
    val standardOfCareMatches: List<AnnotatedTreatmentMatch>?,
    val personalizedDataAnalysis: PersonalizedDataAnalysis?,
    val survivalPredictionsPerTreatment: List<TreatmentEfficacyPrediction>?,
    val maxMolecularTestAge: LocalDate? = null
)

data class TreatmentEfficacyPrediction(
    val treatment: String,
    val survivalProbs: List<Double>,
    val shapValues: Map<String, ShapDetail>
) {
    data class ShapDetail(
        val featureValue: Double,
        val shapValue: Double
    )
}