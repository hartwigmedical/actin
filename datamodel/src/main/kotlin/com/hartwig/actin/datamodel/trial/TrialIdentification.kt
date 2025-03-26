package com.hartwig.actin.datamodel.trial

data class TrialIdentification(
    val trialId: String,
    val open: Boolean,
    val acronym: String,
    val title: String,
    val nctId: String? = null,
    val phase: TrialPhase? = null,
    val source: TrialSource? = null,
    val sourceId: String? = null,
    val locations: List<String> = emptyList(),
    val link : String? = null
)
