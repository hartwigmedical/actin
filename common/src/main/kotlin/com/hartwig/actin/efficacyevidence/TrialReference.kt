package com.hartwig.actin.efficacyevidence

data class TrialReference(
    val patientPopulations: List<PatientPopulation>,
    val url: String
)