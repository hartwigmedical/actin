package com.hartwig.actin.algo.datamodel

import java.time.LocalDate

data class TreatmentMatch(
    val patientId: String,
    val sampleId: String,
    val referenceDate: LocalDate,
    val referenceDateIsLive: Boolean,
    val trialMatches: List<TrialMatch>,
    val standardOfCareMatches: List<AnnotatedTreatmentMatch>? = null
)
