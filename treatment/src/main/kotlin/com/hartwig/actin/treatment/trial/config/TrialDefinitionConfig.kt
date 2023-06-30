package com.hartwig.actin.treatment.trial.config

data class TrialDefinitionConfig(
    override val trialId: String,
    val open: Boolean,
    val acronym: String,
    val title: String
) : TrialConfig