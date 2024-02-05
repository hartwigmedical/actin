package com.hartwig.actin.algo.ckb.datamodel

import com.google.gson.annotations.SerializedName

data class CkbTrialReference(
    val id: Int,
    @SerializedName("patient_populations") val patientPopulations: List<CkbPatientPopulation>,
    val reference: CkbReference
)