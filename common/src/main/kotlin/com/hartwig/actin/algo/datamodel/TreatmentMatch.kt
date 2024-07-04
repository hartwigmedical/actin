package com.hartwig.actin.algo.datamodel

import com.hartwig.actin.personalization.datamodel.PersonalizedDataAnalysis
import java.time.LocalDate

data class TreatmentMatch(
    val patientId: String,
    val sampleId: String,
    val trialSource: String,
    val referenceDate: LocalDate,
    val referenceDateIsLive: Boolean,
    val trialMatches: List<TrialMatch>,
    val standardOfCareMatches: List<AnnotatedTreatmentMatch>? = null,
    val personalizedDataAnalysis: PersonalizedDataAnalysis? = null
)
