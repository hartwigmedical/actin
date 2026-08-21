package com.hartwig.actin.algo.ckb.json

import com.fasterxml.jackson.annotation.JsonProperty

data class CkbTrialReference(
    val id: Int,
    @JsonProperty("patient_populations") val patientPopulations: List<CkbPatientPopulation>,
    val reference: CkbReference
)