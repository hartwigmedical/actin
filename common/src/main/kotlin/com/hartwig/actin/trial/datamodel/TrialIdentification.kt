package com.hartwig.actin.trial.datamodel

data class TrialIdentification(
    val trialId: String,
    val open: Boolean,
    val acronym: String,
    val title: String,
    val nctId: String? = null
)
