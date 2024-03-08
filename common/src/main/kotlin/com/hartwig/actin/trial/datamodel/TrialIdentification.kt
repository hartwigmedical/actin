package com.hartwig.actin.trial.datamodel

data class TrialIdentification(
    val trialId: String,
    val nctId: String?,
    val open: Boolean,
    val acronym: String,
    val title: String,
)
