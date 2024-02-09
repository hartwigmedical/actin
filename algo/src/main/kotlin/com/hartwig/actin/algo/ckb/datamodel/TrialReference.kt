package com.hartwig.actin.algo.ckb.datamodel

data class TrialReference(
    val patientPopulations: Set<PatientPopulation>,
    val url: String
)