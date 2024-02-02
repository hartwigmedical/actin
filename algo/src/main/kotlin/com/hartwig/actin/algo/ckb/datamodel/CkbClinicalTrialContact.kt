package com.hartwig.actin.algo.ckb.datamodel

data class CkbClinicalTrialContact(
    val name: String,
    val email: String,
    val phone: String,
    val phoneExt: String,
    val role: String
)