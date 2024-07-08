package com.hartwig.actin.trial.config

data class TrialDefinitionConfig(
    override val trialId: String,
    val open: Boolean?,
    val acronym: String,
    val title: String,
    val nctId: String?,
    val phase: String?
) : TrialConfig