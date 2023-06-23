package com.hartwig.actin.treatment.trial.config

data class InclusionCriteriaConfig(
    override val trialId: String,
    val referenceIds: Set<String>,
    val appliesToCohorts: Set<String>,
    val inclusionRule: String
) : TrialConfig