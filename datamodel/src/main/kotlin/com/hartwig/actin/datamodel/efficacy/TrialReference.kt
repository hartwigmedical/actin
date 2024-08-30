package com.hartwig.actin.datamodel.efficacy

data class TrialReference(
    val patientPopulations: List<PatientPopulation>,
    val url: String
)