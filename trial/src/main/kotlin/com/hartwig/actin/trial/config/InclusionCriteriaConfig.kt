package com.hartwig.actin.trial.config

data class InclusionCriteriaConfig(
    override val nctId: String,
    val referenceIds: Set<String>,
    val appliesToCohorts: Set<String>,
    val inclusionRule: String
) : TrialConfig