package com.hartwig.actin.datamodel.trial

data class TrialIdentification(
    val nctId: String,
    val open: Boolean,
    val acronym: String,
    val title: String,
    val phase: TrialPhase? = null
)
