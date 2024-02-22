package com.hartwig.actin.algo.ckb.json

data class CkbClinicalTrialLocation(
    val nctId: String,
    val facility: String,
    val city: String,
    val country: String,
    val status: String?,
    val state: String,
    val zip: String,
    val clinicalTrialContacts: List<CkbClinicalTrialContact>
)
