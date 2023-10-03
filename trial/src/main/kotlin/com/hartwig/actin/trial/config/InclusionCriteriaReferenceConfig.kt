package com.hartwig.actin.trial.config

data class InclusionCriteriaReferenceConfig(
    override val trialId: String,
    val referenceId: String,
    val referenceText: String
) : TrialConfig