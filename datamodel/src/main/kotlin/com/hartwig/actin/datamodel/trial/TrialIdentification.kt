package com.hartwig.actin.datamodel.trial

data class TrialIdentification(
    val trialId: String,
    val open: Boolean,
    val acronym: String,
    val title: String,
    val nctId: String?,
    val phase: TrialPhase?,
    val source: TrialSource?,
    val sourceId: String?,
    val locations: Set<String>,
    val url : String?
)
