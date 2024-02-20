package com.hartwig.actin.efficacy

data class TrialReference(
    val patientPopulations: List<PatientPopulation>,
    val url: String
)