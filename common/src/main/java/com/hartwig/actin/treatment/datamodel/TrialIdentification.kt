package com.hartwig.actin.treatment.datamodel

data class TrialIdentification(
    val trialId: String,
    val open: Boolean,
    val acronym: String,
    val title: String
)
