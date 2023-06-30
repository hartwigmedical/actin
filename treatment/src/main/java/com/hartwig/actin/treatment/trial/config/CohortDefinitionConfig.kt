package com.hartwig.actin.treatment.trial.config

data class CohortDefinitionConfig(
    override val trialId: String,
    val cohortId: String,
    val ctcCohortIds: Set<String>,
    val evaluable: Boolean,
    val open: Boolean?,
    val slotsAvailable: Boolean?,
    val blacklist: Boolean,
    val description: String
) : TrialConfig