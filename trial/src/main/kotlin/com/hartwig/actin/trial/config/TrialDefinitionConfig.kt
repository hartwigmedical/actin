package com.hartwig.actin.trial.config

data class TrialDefinitionConfig(
    override val nctId: String,
    val open: Boolean?,
    val acronym: String,
    val title: String,
    val phase: String?
) : TrialConfig