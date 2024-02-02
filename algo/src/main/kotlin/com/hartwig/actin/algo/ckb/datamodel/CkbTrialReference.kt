package com.hartwig.actin.algo.ckb.datamodel

data class CkbTrialReference(
    val id: Int,
    val patientPopulations: List<CkbPatientPopulation>,
    val reference: CkbReference
)