package com.hartwig.actin.algo.ckb.json

import com.google.gson.annotations.SerializedName

data class JsonCkbTrialReference(
    val id: Int,
    @SerializedName("patient_populations") val patientPopulations: List<JsonCkbPatientPopulation>,
    val reference: JsonCkbReference
)