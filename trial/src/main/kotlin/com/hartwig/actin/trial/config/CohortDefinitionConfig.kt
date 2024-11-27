package com.hartwig.actin.trial.config

data class CohortDefinitionConfig(
    override val nctId: String,
    val cohortId: String,
    val externalCohortIds: Set<String>,
    val evaluable: Boolean,
    val open: Boolean?,
    val slotsAvailable: Boolean?,
    val ignore: Boolean,
    val description: String
) : TrialConfig
