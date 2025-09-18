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
    val personalizedTreatmentSummary: PersonalizedTreatmentSummary?,
    val maxMolecularTestAge: LocalDate? = null
)

data class PersonalizedTreatmentSummary(
    val predictions: List<TreatmentEfficacyPrediction>?,
    val similarPatientsSummary: SimilarPatientsSummary?
)
