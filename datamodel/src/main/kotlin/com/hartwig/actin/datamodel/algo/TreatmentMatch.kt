package com.hartwig.actin.datamodel.algo

import java.time.LocalDate

data class TreatmentMatch(
    val patientId: String,
    val referenceDate: LocalDate,
    val referenceDateIsLive: Boolean,
    val trialMatches: List<TrialMatch>,
    val standardOfCareMatches: List<AnnotatedTreatmentMatch>?,
    val personalizedTreatmentSummary: PersonalizedTreatmentSummary?
)

